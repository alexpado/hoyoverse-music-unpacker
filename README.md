# Genshin Audio Unpacker

*Thanks to [MeguminSama project](https://github.com/MeguminSama/genshin-audio-extractor) which has been a huge source of
inspiration for this.*

## Why ?

This project has been made to extract music file from the game Genshin Impact.

Although [genshin-audio-extractor](https://github.com/MeguminSama/genshin-audio-extractor) supports it very well, I
wanted to work on my own version for the challenge and to add few features.

**This program will only process Minimum.pck and Music\*.pck files**

## Ok, but still, why ?

Since 2.5, Mihoyo decided to change the way update are downloaded. Instead of downloading a whole package with all
updated PCK files, they work with `hdiff` files which only update part of the current game files.

This is cleaner, smarter and reduces a lot the download size.

Although, I was using another audio extractor at the time to extract music from the update package, this was now
impossible with `hdiff` files, so I wrote this program which allow me to patch current game file and extract new musics.

## How to use it

From the `--help` output:

```
Usage: [-dhpV] -g=<genshinFolder> [-t=<threadCount>]
  -d, --diff      Extract update package only
  -g, --genshin=<genshinFolder>
                  Installation folder of the game
  -h, --help      Show this help message and exit.
  -p, --prefix    (With --diff) Add status prefix to files
  -t, --threads=<threadCount>
                  Number of parallel thread that can be used.
  -V, --version   Print version information and exit.
```

> Please note that the Genshin Impact directory must be where the game and launcher are installed, not only the game

e.g.: `D:\Program Files\Genshin Impact` not `D:\Program Files\Genshin Impact\Genshin Impact Game`

### Examples

If you want to extract music from the current version of the game:

```bash
java -jar <whateverTheName>.jar --genshin="D:\Program Files\Genshin Impact"
```

If you want to extract music from the update package (once the download though the launcher is finished):

```bash
java -jar <whateverTheName>.jar --genshin="D:\Program Files\Genshin Impact" --diff
```

The previous command will leave a lot of file, both created and updated... If you want to know which one has been
create/update, add the `--prefix` flag:

```
java -jar <whateverTheName>.jar --genshin="D:\Program Files\Genshin Impact" --diff --prefix
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

