package ovh.akio.hmu;

import me.tongfei.progressbar.ProgressBar;
import ovh.akio.hmu.entities.PckAudioFile;
import ovh.akio.hmu.entities.WemAudioFile;
import ovh.akio.hmu.enums.AudioSource;
import ovh.akio.hmu.enums.AudioState;
import ovh.akio.hmu.interfaces.HoyoverseGame;
import ovh.akio.hmu.interfaces.states.Patchable;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@CommandLine.Command(
        name = "extract",
        mixinStandardHelpOptions = true,
        version = "Hoyoverse Audio Extractor 1.2"
)
public class Application implements Callable<Integer> {

    private static final int CODE_OK                   = 0;
    private static final int CODE_DIFF_MODE_IMPOSSIBLE = 1;
    private static final int CODE_NO_UPDATE            = 2;


    @CommandLine.Option(
            names = {"-a", "--all"},
            description = "Search for all valid audio files (not just music)",
            defaultValue = "false"
    )
    private boolean allowAnyAudioFiles;

    @CommandLine.Option(
            names = {"-f", "--filter"},
            description = "Input a custom file filter as regex",
            defaultValue = ""
    )
    private String customFileFilter;

    @CommandLine.Option(
            names = {"-g", "--game"},
            description = "Installation folder of the game",
            required = true
    )
    private File gameFolder;

    @CommandLine.Option(
            names = {"-o", "--output"},
            description = "Output folder for the extracted files",
            defaultValue = ""
    )
    private File outputFolder;

    @CommandLine.Option(
            names = {"-d", "--diff"},
            description = "Extract update package only",
            defaultValue = "false"
    )
    private boolean diffMode;

    @CommandLine.Option(
            names = {"-p", "--prefix"},
            description = "(With --diff) Add status prefix to files",
            defaultValue = "false"
    )
    private boolean prefixEnabled;

    @CommandLine.Option(
            names = {"-t", "--threads"},
            description = "Number of parallel thread that can be used.",
            defaultValue = "4"
    )
    private int threadCount;

    private Predicate<File> activeFileFilter;

    public static void main(String... args) {

        System.exit(new CommandLine(new Application()).execute(args));
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     *
     * @throws Exception
     *         if unable to compute a result
     */
    @Override
    public Integer call() throws Exception {

        System.out.println("Detecting game...");
        HoyoverseGame game = HoyoverseGame.of(this.gameFolder);
        System.out.println("  -- Detected " + game.getName() + " !");

        if (this.allowAnyAudioFiles) {
            this.activeFileFilter = file -> true;
        } else if (customFileFilter.isBlank()) {
            this.activeFileFilter = game.getGameType().getDefaultFileFilter();
        } else {
            Pattern pattern = Pattern.compile(customFileFilter);
            this.activeFileFilter = file -> pattern.asMatchPredicate().test(file.getName());
        }

        if (this.diffMode) {
            return this.differentialExtract(game, this.prefixEnabled);
        } else {
            return this.regularExtract(game);
        }
    }

    private int regularExtract(HoyoverseGame game) {

        GameUnpacker unpacker = new GameUnpacker(game, this.threadCount, this.outputFolder);

        if (game instanceof Patchable patchable && patchable.getUpdatePackageFile().isPresent()) {
            System.out.println("An update package has been detected. Use --diff to extract the update package.");
        }

        // Cleaning up the previous runs
        System.out.println("Removing previous files...");
        Utils.delete(DiskUtils.workspace(game).toFile());

        System.out.println("Starting unpacking...");
        unpacker.unpackFiles(AudioSource.GAME, this.activeFileFilter);
        unpacker.convertFiles(AudioSource.GAME, this.activeFileFilter);

        return CODE_OK;
    }

    private int differentialExtract(HoyoverseGame game, boolean flagFiles) throws IOException, InterruptedException {

        GameUnpacker unpacker = new GameUnpacker(game, this.threadCount, this.outputFolder);

        if (!(game instanceof Patchable patchable)) {
            System.out.println("This game is not compatible with --diff flag.");
            return CODE_DIFF_MODE_IMPOSSIBLE;
        }

        if (patchable.getUpdatePackageFile().isEmpty()) {
            System.out.println("No update package found.");
            return CODE_NO_UPDATE;
        }

        // Cleaning up the previous runs
        System.out.println("Removing previous files...");
        Utils.delete(DiskUtils.workspace(game).toFile());

        unpacker.unpackFiles(AudioSource.GAME, this.activeFileFilter);

        patchable.extractUpdatePackage();
        patchable.patch();

        unpacker.unpackFiles(AudioSource.PATCHED, this.activeFileFilter);
        unpacker.convertFiles(AudioSource.PATCHED, this.activeFileFilter);

        if (!flagFiles) {
            return CODE_OK;
        }

        // Process WEM hashes
        List<WemAudioFile> patchedWem = patchable.getPatchedFiles()
                                                 .stream()
                                                 .flatMap(PckAudioFile::getOutputFiles)
                                                 .toList();

        List<WemAudioFile> originalWem = game.getAudioFiles(this.activeFileFilter)
                                             .stream()
                                             .flatMap(PckAudioFile::getOutputFiles)
                                             .toList();

        Map<WemAudioFile, WemAudioFile> audioFileMapping = new HashMap<>();

        try (ProgressBar pb = Utils.defaultProgressBar("       Mapping", patchedWem.size())) {
            for (WemAudioFile wemAudioFile : patchedWem) {

                Optional<WemAudioFile> optionalAudio = originalWem.stream()
                                                                  .filter(audioFile -> audioFile.getName()
                                                                                                .equals(wemAudioFile.getName()))
                                                                  .findFirst();

                optionalAudio.ifPresent(file -> audioFileMapping.put(wemAudioFile, file));
                pb.step();
            }
        }


        int total = originalWem.size() + patchedWem.size();

        try (ProgressBar pb = Utils.defaultProgressBar("      Indexing", total)) {

            Stream.concat(originalWem.stream(), patchedWem.stream()).forEach(audioFile -> {
                try {
                    audioFile.processHash();
                    pb.step();
                } catch (Exception e) {
                    System.err.println("Failed to process hash for file " + audioFile.getName());
                    throw new RuntimeException(e);
                }
            });
        }

        try (ProgressBar pb = Utils.defaultProgressBar("     Comparing", patchedWem.size())) {
            for (WemAudioFile wemAudioFile : patchedWem) {
                if (audioFileMapping.containsKey(wemAudioFile)) {
                    WemAudioFile other = audioFileMapping.get(wemAudioFile);
                    if (wemAudioFile.getHash().equals(other.getName())) {
                        wemAudioFile.setState(AudioState.UNCHANGED);
                    } else {
                        wemAudioFile.setState(AudioState.UPDATED);
                    }
                } else {
                    if (originalWem.stream().anyMatch(file -> file.getHash().equals(wemAudioFile.getHash()))) {
                        wemAudioFile.setState(AudioState.DUPLICATED);
                    } else {
                        wemAudioFile.setState(AudioState.CREATED);
                    }
                }

                pb.step();
            }
        }

        try (ProgressBar pb = Utils.defaultProgressBar("      Renaming", patchedWem.size())) {
            for (WemAudioFile wemAudioFile : patchedWem) {

                File output = wemAudioFile.getOutput();

                if (output != null) {

                    File newFile = new File(
                            output.getParent(),
                            String.format("[%s] %s.wav", wemAudioFile.getState().getFlag(), wemAudioFile.getName())
                    );

                    if (output.renameTo(newFile)) {
                        wemAudioFile.onHandled(newFile);
                    }
                }

                pb.step();
            }
        }

        return CODE_OK;
    }

}
