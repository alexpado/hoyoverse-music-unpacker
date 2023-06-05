# Hoyoverse Audio Unpacker

> *Thanks to [MeguminSama's project](https://github.com/MeguminSama/genshin-audio-extractor) which has been a huge
source of
inspiration for this.*

1. The project
2. How to extract musics
    1. Requirements and installation
    2. How to use it
3. About update packages
4. Contributing
5. Feedbacks
6. External executables

# 1. The project

This project was made to extract music file from Genshin Impact, but later updated to support other Hoyoverse's title.

This program will only process PCK files that are known to contains mainly music, as extracting everything would also
include voice lines and SFX. Maybe I'll add an option later to allow to extract everything, but please note that it will
generate ***a lot*** of files (meaning a lot of disk space and time would be required).

# 2. How to extract musics

| Game             | Music Extraction                                                                 | Update Package Support |
|------------------|----------------------------------------------------------------------------------|------------------------|
| Genshin Impact   | ✅ Supported                                                                      | ✅ Supported            |
| Honkai Star Rail | ✅ Supported                                                                      | Not supported          |
| Honkai Impact    | [Partial Support](https://github.com/alexpado/hoyoverse-music-unpacker/issues/2) | ✅ Supported            |

## Requirements and installation

1. Download and
   install [Java 17 or newer from the official website](https://www.oracle.com/java/technologies/downloads/).
2. Download the extractor from the [release page](https://github.com/alexpado/hoyoverse-music-unpacker/releases).
3. Extract the zip file downloaded anywhere on your computer. The directory does not matter.

## How to use it

You'll need to use the terminal to use the extractor. If you're not familiar on how to open the terminal in the
directory you are in:

1. Open the directory where the `hoyoverse-music-unpacker.jar` file and the `wrappers` directory are.
2. Hold the `Shift` key and right-click on an empty space in the directory.
3. Select the `Open PowerShell window here` (label might vary slightly depending on your Windows version)

```
  -d, --diff                Extract update package only
  -g, --game=<gameFolder>   Installation folder of the game
  -h, --help                Show this help message and exit.
  -o, --output=<outputFolder>
                            Output folder for the extracted files
  -p, --prefix              (With --diff) Add status prefix to files
  -t, --threads=<threadCount>
                            Number of parallel thread that can be used.
  -V, --version             Print version information and exit.
```

**Important:** Please note that `--game` option must be the path leading to the launcher, not directly the game. Here is
a table to better explain what I mean (using my own install path, but you'll get the idea):

| Game             | Right Path (launcher)          | Wrong path (game)                           |
|------------------|--------------------------------|---------------------------------------------|
| Genshin Impact   | D:\Games\Genshin Impact        | D:\Games\Genshin Impact\Genshin Impact game |
| Honkai Star Rail | D:\Games\Star Rail             | D:\Games\Star Rail\Games                    |
| Honkai Impact    | D:\Games\Honkai Impact 3rd glb | D:\Games\Honkai Impact 3rd glb\Games        |

### Extracting all musics

```bash
java -jar hoyoverse-music-unpacker.jar --game="D:\Games\Genshin Impact"
```

*You have to replace the path of `--game` by your own. `"` around the game path are important !*

Once finished, all musics will be present in the `extracted` folder.

You can also change the output directory by using the `--output` option. Example:

```bash
java -jar hoyoverse-music-unpacker.jar --game="D:\Games\Genshin Impact" --output="D:\Documents\Musics"
```

This will extract all music in `D:\Documents\Musics` (a subdirectory will be created for each game).

# 3. About update packages

When a game is going to be updated in the next few days, Hoyoverse allows player to download the update package before
the actual release. This package contains the whole update and can be used to retrieve the music before the game is
updated.

This is working by doing the same job as the launcher when applying the update. **This won't be done on your game
files !** When patching, it does not modify the original file, but creates a new one, avoiding breaking your game.

### Steps for extracting the update package (skip if you don't care)

1. Unpack PCK files to workspace (PCK -> WEM)
2. Unzip update zip package to workspace
3. Patch PCK files using update package (skipped for Honkai Impact which is not using HDIFF files)
4. Unpack patched PCK files (PCK -> WEM)
5. Convert WEM to WAV
6. Index files from step 2 and 6
7. Compare files and rename if --prefix

> ❌ **The update package extraction feature is going under rewrite. Please check the compatibility table above for
further details.**

If you want to extract music from the update package (once the download through the launcher is finished), you have to
use the `--diff` flag:

```bash
java -jar hoyoverse-music-unpacker.jar --game="D:\Games\Genshin Impact" --diff
```

This will leave a lot of file, both created and updated... If you want to know which one has been
created/updated, add the `--prefix` flag:

```bash
java -jar hoyoverse-music-unpacker.jar --game="D:\Games\Genshin Impact" --diff --prefix
```

Every file will now have a prefix:

- **[-]**: The file did not change compared to the current version
- **[C]**: The file has been created with this update
- **[U]**: The file has been updated with this update
    - *You most likely already heard it, but you can check just in case*
- **[D]**: The file has never been seen, but is very similar to an already existing one within the game.
    - *This happens way more often than you think...*

# 4. Contributing

I'm open for contribution, but if your contribution is a new feature instead of a
bugfix, it may be wise to open a discussion [here](https://github.com/alexpado/hoyoverse-music-unpacker/discussions)
before to avoid any potential waste of time.

# 5. Feedback

For feature requests and question, please open a
discussion [here](https://github.com/alexpado/hoyoverse-music-unpacker/discussions), for bug report please open an
issue.

# 6. External executables

- **QuickBMS:** Used to unpack PCK files to WEM files
- **VGMStream:** Used to convert WEM files to WAV files
- **hdiffpatch:** Used to patch PCK files with the `hdiff` files in the update package

