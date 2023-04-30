# Hoyoverse Audio Unpacker

> *Thanks to [MeguminSama's project](https://github.com/MeguminSama/genshin-audio-extractor) which has been a huge
source of
inspiration for this.*

## Game Tested

- Genshin Impact
- Honkai Star Rails
- Honkai Impact (but read more [here](https://github.com/alexpado/hoyoverse-music-unpacker/issues/2) before)

## Why ?

This project was made to extract music file from Genshin Impact, but later updated to support other Hoyoverse's title.

This program will only process PCK files that are known to contains mainly music, as extracting everything would also
include voice lines and SFX. Maybe I'll add an option later to allow to extract everything, but please note that it will
generate ***a lot*** of files.

## Installation

You'll need to install Java (at least version 17) for this program to work.
Here's [the official download website](https://www.oracle.com/java/technologies/downloads/).

You can then download the `hoyoverse-music-unpacker.zip` file from
the [release page](https://github.com/alexpado/hoyoverse-music-unpacker/releases).

Extract the zip anywhere, open the program directory (where there is the `jar` file and the `wrappers`
directory), `Shift+Right Click` on an empty space and click `Open PowerShell window here` (label might vary slightly
depending on your windows version)

## How to use it

```
  -d, --diff                Extract update package only
  -g, --game=<gameFolder>   Installation folder of the game
  -h, --help                Show this help message and exit.
  -p, --prefix              (With --diff) Add status prefix to files
  -t, --threads=<threadCount>
                            Number of parallel thread that can be used.
  -V, --version             Print version information and exit.
```

> *Please note that the game directory must be where the launcher is, not the game !*

Here is a table to better explain what I mean (using my own install path, but you'll get the idea):

| Game             | Right Path (launcher)          | Wrong path (game)                           |
|------------------|--------------------------------|---------------------------------------------|
| Genshin Impact   | D:\Games\Genshin Impact        | D:\Games\Genshin Impact\Genshin Impact game |
| Honkai Star Rail | D:\Games\Star Rail             | D:\Games\Star Rail\Games                    |
| Honkai Impact    | D:\Games\Honkai Impact 3rd glb | D:\Games\Honkai Impact 3rd glb\Games        |

### Normal Usage: Extracting all musics

> Launching the extraction process will remove all files previously extracted for the selected game ! Please make sure
> you have nothing left in these folder before starting !

If you want to extract music from the current version of the game:

```bash
java -jar hoyoverse-music-unpacker.jar --game="D:\Games\Genshin Impact"
```

*You have to replace the path of `--game` by your own. `"` around the game path are important !*

Once finished, all musics will be present in the `extracted` folder.

## Advanced Usage: Extracting update package
> This has been tested and done only with Genshin Impact for now. Honkai Star Rail & Honkai Impact aren't supported.

If you want to extract music from the update package (once the download through the launcher is finished):

```bash
java -jar hoyoverse-music-unpacker.jar --game="D:\Games\Genshin Impact" --diff
```

This will leave a lot of file, both created and updated... If you want to know which one has been
created/updated, add the `--prefix` flag:

```bash
java -jar hoyoverse-music-unpacker.jar --game="D:\Games\Genshin Impact" --diff --prefix
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

