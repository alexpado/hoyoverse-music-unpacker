# Hoyoverse Audio Unpacker

*Thanks to [MeguminSama project](https://github.com/MeguminSama/genshin-audio-extractor) which has been a huge source of
inspiration for this.*

## Game Tested

- Genshin Impact
- Honkai Star Rails

## Why ?

This project has been made to extract music file from the game Genshin Impact, but later updated to support Honkai Star
Rails.

**This program will only process Minimum.pck and Music\*.pck files**

## How to use it

From the `--help` output:

```
Usage: [-dhpV] -g=<genshinFolder> [-t=<threadCount>]
  -d, --diff      Extract update package only
  -g, --game=<gameFolder>
                  Installation folder of the game
  -h, --help      Show this help message and exit.
  -p, --prefix    (With --diff) Add status prefix to files
  -t, --threads=<threadCount>
                  Number of parallel thread that can be used.
  -V, --version   Print version information and exit.
```

> Please note that the game directory must be where the game and launcher are installed, not only the game

e.g.: `D:\Program Files\Genshin Impact` not `D:\Program Files\Genshin Impact\Genshin Impact Game`
e.g.: `D:\Program Files\Star Rail` not `D:\Program Files\Star Rail\Games`

### Examples

If you want to extract music from the current version of the game:

```bash
java -jar <whateverTheName>.jar --game="D:\Program Files\Genshin Impact"
```

If you want to extract music from the update package (once the download through the launcher is finished):

```bash
java -jar <whateverTheName>.jar --game="D:\Program Files\Genshin Impact" --diff
```

> Please note that the update package stuff has been done only for Genshin. I'll wait for the first update of HSR to add it.

The previous command will leave a lot of file, both created and updated... If you want to know which one has been
created/updated, add the `--prefix` flag:

```
java -jar <whateverTheName>.jar --game="D:\Program Files\Genshin Impact" --diff --prefix
```

Every file will now have a prefix:

- **[C]**: The file has been created with this update
- **[U]**: The file has been updated with this update
    - *You most likely already heard it, but you can check just in case*
- **[D]**: The file has never been seen, but is very similar to an already existing one within the game.
    - *This happens way more often than you think...*

### Feature Requests, Bug Report

I might not look too much on feature requests, but surely on bug reports. Though for features, I'm open to PR, just open
an issue beforehand to talk about it.

### External Programs

- **QuickBMS:** Used to unpack PCK files to WEM files
- **VGMStream:** Used to convert WEM files to WAV files
- **hdiffpatch:** Used to patch PCK files with the `hdiff` files in the update package

