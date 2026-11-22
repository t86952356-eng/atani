# atani-final

A final version of Atani client with bugs, exploits, and useless code removed.

From this version forwards, only bug fixes will be added, not any features.

## Running from source (dev)

The LWJGL natives folder is no longer shipped with the repository (it was 26MB of dev-only files
and is not part of the built client). To run `Start` locally, download the LWJGL 2.9.4-nightly
natives for your platform into a `natives/<windows|linux>` folder next to the repository, or point
to any folder using `-Dnatives.path=...`. End users don't need this: the released client jar gets
its natives from the launcher / the version JSON, exactly like vanilla 1.8.9.

## FCL / Android notes

The client is built for Minecraft Client Version installs (standalone version jar, vanilla 1.8.9
libraries from the version JSON). Two launchers put those libraries **before** the client jar on
the classpath (FCL, Pojav), which drives these rules:

- The jar must never bundle code that conflicts with the classpath libraries. In particular the
  slf4j binding is `slf4j-simple` (stderr) — do NOT add `log4j-slf4j2-impl` or bundle log4j-api /
  log4j-core again: vanilla 1.8.9 supplies log4j 2.0-beta9 on the classpath and a newer bundled
  bridge crashes at the Mojang logo with
  `NoSuchMethodError: LoggerContextFactory.isClassLoaderDependent()`.
- `DiscordRP` is hardened: on devices without discord-rpc natives (e.g. Android arm64) the Rich
  Presence disables itself instead of crashing startup.
- After `gradlew shadowJar`, CI runs `python tools/fcl_slim.py build/libs/atani-client-1.8.9-all.jar`
  which removes entries shadowed by the version-JSON libraries (they can never be loaded from the
  jar anyway) plus lossless PNG/JSON recompression. This cuts the jar size substantially with
  zero behavioral change. `tools/gen_cplist.py` regenerates the path list if the library set in
  `release/atani-client-1.8.9-all.json` ever changes.

