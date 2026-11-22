#!/usr/bin/env python3
"""يولّد tools/fcl_classpath_paths.txt — اتحاد مسارات مدخلات مكتبات classpath
الفانيلا 1.8.9 (نفس قائمة release/atani-client-1.8.9-all.json التي يضعها FCL
على الـ classpath قبل جار العميل). أي مدخل في جار العميل مساره هنا محجوب
دائماً بنسخة الـ classpath — حذفه لا يغيّر السلوك.

يعمل مرة واحدة محلياً؛ الناتج يُرفَع مع الريبو حتى لا تحتاج الـ CI لتنزيل
المكتبات.

ملاحظة: lwjgl مقصودة هنا عمداً — FCL يستبدلها بـ lwjgl-boat الخاصة به،
ونُبقي نسخة الجار كشبكة أمان لأي فجوة في تغطية boat.
"""
import os
import zipfile

LIBS = [
    "netty-1.6.jar",
    "vecmath-1.5.2.jar",
    "trove4j-3.0.3.jar",
    "icu4j-core-mojang-51.2.jar",
    "jopt-simple-4.6.jar",
    "codecjorbis-20101023.jar",
    "codecwav-20101023.jar",
    "libraryjavasound-20101123.jar",
    "librarylwjglopenal-20100824.jar",
    "soundsystem-20120107.jar",
    "netty-all-4.0.15.Final.jar",
    "guava-17.0.jar",
    "commons-lang3-3.3.2.jar",
    "commons-io-2.4.jar",
    "commons-codec-1.9.jar",
    "jinput-2.0.5.jar",
    "jutils-1.0.0.jar",
    "gson-2.2.4.jar",
    "authlib-1.5.21.jar",
    "realms-1.6.1.jar",
    "commons-compress-1.8.1.jar",
    "httpclient-4.3.3.jar",
    "commons-logging-1.1.3.jar",
    "httpcore-4.3.2.jar",
    "log4j-api-2.0-beta9.jar",
    "log4j-core-2.0-beta9.jar",
    "twitch-6.5.jar",
]

CP_DIR = os.environ.get("CP_DIR", "/home/z/my-project/work/cp-libs")
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "fcl_classpath_paths.txt")

paths = set()
for lib in LIBS:
    p = os.path.join(CP_DIR, lib)
    if not os.path.exists(p):
        raise SystemExit("missing lib: " + p)
    with zipfile.ZipFile(p) as z:
        for n in z.namelist():
            if n.endswith("/"):
                continue
            if n == "META-INF/MANIFEST.MF":
                continue
            paths.add(n)

with open(OUT, "w", encoding="utf-8") as f:
    for n in sorted(paths):
        f.write(n + "\n")
print("wrote %s: %d paths from %d libs" % (OUT, len(paths), len(LIBS)))
