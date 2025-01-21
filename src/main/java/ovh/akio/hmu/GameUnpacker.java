package ovh.akio.hmu;

import me.tongfei.progressbar.ProgressBar;
import ovh.akio.hmu.entities.PckAudioFile;
import ovh.akio.hmu.entities.WemAudioFile;
import ovh.akio.hmu.enums.AudioSource;
import ovh.akio.hmu.exceptions.ConverterProgramException;
import ovh.akio.hmu.interfaces.AudioConverter;
import ovh.akio.hmu.interfaces.AudioFile;
import ovh.akio.hmu.interfaces.HoyoverseGame;
import ovh.akio.hmu.interfaces.states.Patchable;
import ovh.akio.hmu.wrappers.Pck2Wem;
import ovh.akio.hmu.wrappers.Wem2Wav;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

public class GameUnpacker {

    private final File            outputDir;
    private final HoyoverseGame   game;
    private final ExecutorService service;

    public GameUnpacker(HoyoverseGame game, int maxThreads, File outputDir) {

        this.game    = game;
        this.service = Executors.newFixedThreadPool(Math.max(maxThreads, 1));

        // If an output dir is selected, use it. Otherwise return the current directory
        if (!outputDir.getName().isEmpty()) {
            this.outputDir = outputDir;
        } else {
            this.outputDir = new File(".", "extracted");
        }
    }

    private <T extends AudioFile> void run(String name, Path output, AudioConverter<T> converter, Collection<T> files) {

        try (ProgressBar pb = Utils.defaultProgressBar(name, files.size())) {

            CompletableFuture<?>[] futures = files.stream()
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
                    System.out.printf("%n%n%n");
                    System.err.println("An error occurred while extracting " + ex.getAudioFile()
                                                                                 .getName() + " (code " + ex.getCode() + ")");
                    System.out.println(" ==== Program Output ====");
                    System.out.println(ex.getOutput());
                    System.out.println(" ========================");
                    System.out.println(" ==== Program Error =====");
                    System.out.println(ex.getError());
                    System.out.println(" ========================");
                    System.out.printf("%n%n%n");
                }
                return null;
            }).join();
            pb.setExtraMessage("OK");
        }
    }

    private List<WemAudioFile> getWemAudioFiles(AudioSource source, Predicate<File> filter) {

        return switch (source) {
            case GAME -> this.game.getAudioFiles(filter).stream().flatMap(PckAudioFile::getOutputFiles).toList();
            case PATCHED -> ((Patchable) this.game).getPatchedFiles()
                                                   .stream()
                                                   .flatMap(PckAudioFile::getOutputFiles)
                                                   .toList();
        };
    }

    public void unpackFiles(AudioSource source, Predicate<File> filter) {

        switch (source) {
            case GAME -> {
                this.run("     Unpacking", DiskUtils.unpack(this.game), new Pck2Wem(), this.game.getAudioFiles(filter));
            }
            case PATCHED -> {
                if (this.game instanceof Patchable patchableGame) {
                    this.run("     Unpacking", DiskUtils.unpackUpdate(this.game), new Pck2Wem(), patchableGame.getPatchedFiles());
                    return;
                }
                throw new IllegalStateException("Unable to unpack with PATCHED source.");
            }
        }
    }

    public void convertFiles(AudioSource source, Predicate<File> filter) {

        this.run(
                "    Extracting",
                DiskUtils.extracted(this.game, this.outputDir),
                new Wem2Wav(),
                this.getWemAudioFiles(source, filter)
        );
    }

}
