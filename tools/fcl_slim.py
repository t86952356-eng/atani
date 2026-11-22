#!/usr/bin/env python3
"""تقليل حجم جار atani-client-1.8.9-all لبيئة FCL بدون أي تغيير في السلوك.

المبدأ الحاكم: مشغّلات Client Version (FCL/Pojav) تضع مكتبات 1.8.9 الفانيلا
على الـ classpath قبل جار العميل (انظر ترتيب -cp في سجل FCL). قاعدة JVM:
لأي مسار كلاس/مورد موجود في مكتبة classpath وفي الجار معاً، نسخة الـ classpath
هي التي تُحمَّل دائماً. إذن حذف أي مدخل من الجار مساره موجود في إحدى مكتبات
الـ classpath المعروفة لا يغيّر سلوك العميل على الإطلاق — كان أصلاً غير قابل
للوصول (dead weight).

ما يفعله هذا السكربت:
  1) حذف المدخلات المطابقة لمسارات مكتبات classpath الفانيلا
     (القائمة في fcl_classpath_paths.txt — نفس قائمة release/*.json).
  2) حذف ملفات التواقيع META-INF/*.SF|*.RSA|*.DSA|*.EC إن وجدت.
  3) PNG: إعادة ضغط IDAT فقط (مستوى 9 ثم 6 وأخذ الأصغر) مع تحقق
     bit-for-bit أن البكسلات المفكوكة متطابقة قبل وبعد.
  4) JSON/.mcmeta: تصغير بلا فقدان (json.loads/dumps) مع تحقق إعادة التحليل.
  5) إعادة كتابة الجار كاملاً بضغط deflate مستوى 9 (بدون تكرارات،
     وبنفس ترتيب المدخلات).
  6) تقرير مفصّل بالأحجام قبل/بعد، ولا يقبل الفشل الصامت:
     أي خطأ تحقق = استثناء = فشل البناء.

الاستخدام: python tools/fcl_slim.py <path-to-fat-jar>
"""
import json
import os
import struct
import sys
import zlib
import zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
PATHS_FILE = os.path.join(HERE, "fcl_classpath_paths.txt")
PNG_SIG = b"\x89PNG\r\n\x1a\n"
SIG_SUFFIXES = (".SF", ".RSA", ".DSA", ".EC")


def load_removal_set():
    with open(PATHS_FILE, "r", encoding="utf-8") as f:
        return set(line.rstrip("\n") for line in f if line.strip())


def recompress_png(data: bytes):
    """يعيد ضغط IDAT فقط. يعيد البايتات الجديدة أو None إن لم تُفد/لم تكن صالحة."""
    if not data.startswith(PNG_SIG):
        return None
    pos = 8
    chunks = []
    idat = b""
    while pos + 8 <= len(data):
        ln, typ = struct.unpack(">I4s", data[pos:pos + 8])
        payload = data[pos + 8:pos + 8 + ln]
        crc = data[pos + 8 + ln:pos + 12 + ln]
        if len(payload) != ln or len(crc) != 4:
            return None  # بنية غير متوقعة — لا نلمس
        chunks.append((typ, payload))
        if typ == b"IDAT":
            idat += payload
        pos += 12 + ln
    if pos != len(data) or not idat:
        return None
    try:
        raw = zlib.decompress(idat)
    except Exception:
        return None
    best = None
    for level in (9, 6):
        cand = zlib.compress(raw, level)
        if best is None or len(cand) < len(best):
            best = cand
    if best is None or len(best) >= len(idat):
        return None  # لا فائدة
    # ضمان نهائي: البيانات المفكوكة من الضغط الجديد مطابقة تماماً
    if zlib.decompress(best) != raw:
        return None

    out = bytearray(PNG_SIG)
    remaining = best
    first_idat = True
    for typ, payload in chunks:
        if typ == b"IDAT":
            if first_idat:
                out += struct.pack(">I4s", len(remaining), b"IDAT")
                out += remaining
                out += struct.pack(">I", zlib.crc32(b"IDAT" + remaining) & 0xFFFFFFFF)
                first_idat = False
        else:
            out += struct.pack(">I4s", len(payload), typ)
            out += payload
            out += struct.pack(">I", zlib.crc32(typ + payload) & 0xFFFFFFFF)
    return bytes(out)


def minify_json(raw: bytes):
    if raw.startswith(b"\xef\xbb\xbf"):
        return None  # BOM — لا نلمس
    try:
        obj = json.loads(raw.decode("utf-8"))
        mini = json.dumps(obj, separators=(",", ":"), ensure_ascii=False).encode("utf-8")
    except Exception:
        return None
    if len(mini) >= len(raw):
        return None
    # ضمان: إعادة تحليل النص المصغر تعطي نفس الكائن تماماً
    if json.loads(mini.decode("utf-8")) != obj:
        return None
    return mini


def top_pkg(name):
    parts = name.split("/")
    return parts[0] if len(parts) == 1 else "/".join(parts[:2])


def main():
    if len(sys.argv) != 2:
        print(__doc__)
        return 2
    jar_path = sys.argv[1]
    removal = load_removal_set()
    size_before = os.path.getsize(jar_path)

    src = zipfile.ZipFile(jar_path, "r")
    seen = set()
    removed_bytes = {}
    removed_n = 0
    png_saved = png_n = json_saved = json_n = 0
    out_entries = []  # (name, data, is_dir)

    for info in src.infolist():
        name = info.filename
        if name in seen:
            continue  # تكرار داخل الجار — نُبقي الأول (نفس سلوك JVM)
        seen.add(name)
        is_dir = name.endswith("/")
        if is_dir:
            out_entries.append((name, b"", True))
            continue
        base = name.rsplit("/", 1)[-1]
        if name.startswith("META-INF/") and base.endswith(SIG_SUFFIXES):
            removed_bytes["(signature)"] = removed_bytes.get("(signature)", 0) + info.file_size
            removed_n += 1
            continue
        if name != "META-INF/MANIFEST.MF" and name in removal:
            removed_bytes[top_pkg(name)] = removed_bytes.get(top_pkg(name), 0) + info.file_size
            removed_n += 1
            continue
        data = src.read(name)
        if base.endswith(".png"):
            nd = recompress_png(data)
            if nd is not None:
                png_saved += len(data) - len(nd)
                png_n += 1
                data = nd
        elif base.endswith((".json", ".mcmeta")) and not name.startswith("META-INF/"):
            nd = minify_json(data)
            if nd is not None:
                json_saved += len(data) - len(nd)
                json_n += 1
                data = nd
        out_entries.append((name, data, False))

    tmp = jar_path + ".slim"
    with zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED, compresslevel=9) as out:
        for name, data, is_dir in out_entries:
            if is_dir:
                zi = zipfile.ZipInfo(name)
                zi.external_attr = 0x10
                out.writestr(zi, b"")
            else:
                out.writestr(name, data)
    src.close()

    # ===== تحقق نهائي شامل =====
    with zipfile.ZipFile(tmp) as chk:
        for name, data, is_dir in out_entries:
            if is_dir:
                continue
            got = chk.read(name)
            if got != data:
                raise SystemExit("VERIFY FAIL: entry corrupted: " + name)

    os.replace(tmp, jar_path)
    size_after = os.path.getsize(jar_path)

    print("=== FCL slim report: %s ===" % os.path.basename(jar_path))
    print("entries: kept %d, removed %d" % (len(out_entries), removed_n))
    print("size: %.2f MB -> %.2f MB  (saved %.2f MB, %.1f%%)"
          % (size_before / 1048576, size_after / 1048576,
             (size_before - size_after) / 1048576,
             100.0 * (size_before - size_after) / size_before))
    print("lossless PNG recompress: %d files, %.2f MB saved" % (png_n, png_saved / 1048576))
    print("lossless JSON minify:    %d files, %.2f MB saved" % (json_n, json_saved / 1048576))
    print("removed shadowed entries (uncompressed MB, by package):")
    for k in sorted(removed_bytes, key=lambda x: -removed_bytes[x]):
        print("  %-28s %8.2f MB" % (k, removed_bytes[k] / 1048576))
    print("OK")


if __name__ == "__main__":
    sys.exit(main())
