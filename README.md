# atani-final

A final version of Atani client with bugs, exploits, and useless code removed.

From this version forwards, only bug fixes will be added, not any features.

## Running from source (dev)

The LWJGL natives folder is no longer shipped with the repository (it was 26MB of dev-only files
and is not part of the built client). To run `Start` locally, download the LWJGL 2.9.4-nightly
natives for your platform into a `natives/<windows|linux>` folder next to the repository, or point
to any folder using `-Dnatives.path=...`. End users don't need this: the released client jar gets
its natives from the launcher / the version JSON, exactly like vanilla 1.8.9.
