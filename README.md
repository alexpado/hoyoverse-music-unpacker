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

# 2. How to extract musics

| Game             | File Extraction | Update Package Support |
|------------------|-----------------|------------------------|
| Genshin Impact   | ✅ Supported     | Support Removed        |
| Honkai Star Rail | ✅ Supported     | Support Removed        |
| Honkai Impact    | ✅ Supported     | Support Removed        |

*More about update package support [here](#3-about-update-packages)*

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
  -a, --all                 Search for all valid audio files (not just music)
  -f, --filter=<customFileFilter>
                            Input a custom file filter as regex
  -g, --game=<gameFolder>   Installation folder of the game
  -h, --help                Show this help message and exit.
  -o, --output=<outputFolder>
                            Output folder for the extracted files
  -t, --threads=<threadCount>
                            Number of parallel thread that can be used.
  -V, --version             Print version information and exit.
```

*Note: `--all` and `--filter` cannot be used at the same time. When both are supplied, --filter will be ignored, giving
priority to --all*

**Important:** Please note that `--game` option must be the path leading to the **game**, not the launcher. Here is
a table to better explain what I mean (using my own install path, but you'll get the idea):

| Game             | Wrong Path (launcher)          | Right path (game)                           |
|------------------|--------------------------------|---------------------------------------------|
| Genshin Impact   | D:\Games\Genshin Impact        | D:\Games\Genshin Impact\Genshin Impact game |
| Honkai Star Rail | D:\Games\Star Rail             | D:\Games\Star Rail\Games                    |
| Honkai Impact    | D:\Games\Honkai Impact 3rd glb | D:\Games\Honkai Impact 3rd glb\Games        |

> You might have a different folder name with the new launcher, but the rule is you have to select the folder containing
> the game executable.

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

This will extract all music in `D:\Documents\Musics`.

# 3. About update packages

> [!IMPORTANT]
> Since HMU version 1.5, support for update package has been dropped.
> 
> Hoyoverse changed drastically their way of handling pre-download which made this program incompatible, and I don't 
> have the time nor the motivation to search for a solution. You are welcome to try and open a pull request though.

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

