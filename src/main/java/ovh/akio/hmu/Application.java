package ovh.akio.hmu;

import me.tongfei.progressbar.ProgressBar;
import ovh.akio.hmu.entities.PckAudioFile;
import ovh.akio.hmu.entities.WemAudioFile;
import ovh.akio.hmu.exceptions.ConverterProgramException;
import ovh.akio.hmu.interfaces.AudioConverter;
import ovh.akio.hmu.interfaces.AudioFile;
import ovh.akio.hmu.interfaces.HoyoverseGame;
import ovh.akio.hmu.wrappers.Pck2Wem;
import ovh.akio.hmu.wrappers.Wem2Wav;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@CommandLine.Command(
        name = "extract",
        mixinStandardHelpOptions = true,
        version = "Hoyoverse Audio Extractor 1.2"
)
public class Application implements Callable<Integer> {

    private static final int CODE_OK        = 0;
    private static final int CODE_FAILURE   = 3;

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
            names = {"-t", "--threads"},
            description = "Number of parallel thread that can be used.",
            defaultValue = "4"
    )
    private int             threadCount;
    private ExecutorService service;

    public static void main(String... args) {

        System.exit(new CommandLine(new Application()).execute(args));
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     */
    @Override
    public Integer call() {

        this.service = Executors.newFixedThreadPool(Math.max(this.threadCount, 1));

        Logger.info("Detecting game...");
        HoyoverseGame game;

        try {
            game = HoyoverseGame.of(this.gameFolder);
        } catch (Exception e) {
            Logger.error("Could not detect which game the path belongs.");
            Logger.warn(" -> Please double check that the path you entered is not the launcher.");
            Logger.info(" -> https://github.com/alexpado/hoyoverse-music-unpacker/blob/master/README.md");
            return CODE_FAILURE;
        }

        Logger.info("Detected %s game.", game.getName());

        Predicate<File> activeFileFilter;
        if (this.allowAnyAudioFiles) {
            activeFileFilter = file -> true;
            Logger.warn("The '--all' flag has been specified.");
            Logger.warn(" -> All audio files will be extracted, including SFX and Voice overs files.");
            Logger.warn(" -> This **will** take time and disk space.");
        } else if (this.customFileFilter.isBlank()) {
            activeFileFilter = game.getGameType().getDefaultFileFilter();
        } else {
            Pattern pattern = Pattern.compile(this.customFileFilter);
            activeFileFilter = file -> pattern.asMatchPredicate().test(file.getName());
            Logger.warn("The '--filter' flag has been specified.");
            Logger.warn(" -> Audio files will be filtered using the user-provided filter: %s", this.customFileFilter);
            Logger.warn(" -> Depending on the amount of files it matches, the extraction might take a while.");
        }

        if (this.outputFolder.getName().isEmpty()) {
            this.outputFolder = new File("extracted");
        }

        Logger.info("Extracted files will be saved to %s", this.outputFolder.getAbsolutePath());

        Path unpackPath    = AppUtils.Paths.unpack(game);
        Path extractedPath = AppUtils.Paths.extracted(game, this.outputFolder);
        File unpackFile    = unpackPath.toFile();

        if (unpackFile.exists()) {
            Logger.info("Cleaning up old workspace...");
            AppUtils.deleteRecursively(unpackFile);
        }

        List<PckAudioFile> audioFiles = game.getAudioFiles(activeFileFilter);
        this.run("Unpacking", unpackPath, new Pck2Wem(), audioFiles);
        List<WemAudioFile> wemAudioFiles = audioFiles.stream().flatMap(PckAudioFile::getOutputFiles).toList();
        this.run("Extracting", extractedPath, new Wem2Wav(), wemAudioFiles);

        Logger.info("Cleaning up...");
        AppUtils.deleteRecursively(unpackFile);
        return CODE_OK;
    }

    private <T extends AudioFile> void run(CharSequence name, Path output, AudioConverter<T> converter, Collection<T> files) {

        try (ProgressBar pb = AppUtils.createDefaultProgressBar(name, files.size())) {
            CompletableFuture<?>[] futures = files
                    .stream()
                    .map(audio -> (Runnable) () -> {
                        try {
                            File out = converter.handle(audio, output.toFile());
                            audio.onHandled(out);
                            pb.step();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }).map(runnable -> CompletableFuture.runAsync(runnable, this.service))
                    .toArray(CompletableFuture[]::new);

            CompletableFuture.allOf(futures).exceptionally(e -> {
                if (e.getCause() instanceof ConverterProgramException ex) {
                    Logger.error("Audio %s could not be extracted.");
                    Logger.error(" -> File: %s", ex.getAudioFile().getName());
                    Logger.error(" -> Code: %s", ex.getCode());
                    Logger.error(" -> Error: %s", ex.getError());
                    Logger.error(" -> Message: %s", ex.getMessage());
                }
                return null;
            }).join();
            pb.setExtraMessage("OK");
        }
    }

}
