package ovh.akio.hmu;

import me.tongfei.progressbar.ProgressBar;
import ovh.akio.hmu.entities.PckAudioFile;
import ovh.akio.hmu.entities.WemAudioFile;
import ovh.akio.hmu.interfaces.AudioConverter;
import ovh.akio.hmu.interfaces.AudioFile;
import ovh.akio.hmu.interfaces.HoyoverseGame;
import ovh.akio.hmu.wrappers.Pck2Wem;
import ovh.akio.hmu.wrappers.Wem2Wav;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameUnpacker {

    private final File               outputDir;
    private final HoyoverseGame      game;
    private final ExecutorService    service;
    private final List<PckAudioFile> pckAudioFiles;
    private final List<WemAudioFile> wemAudioFiles;

    public GameUnpacker(HoyoverseGame game, int maxThreads, File outputDir) {

        this.game          = game;
        this.service       = Executors.newFixedThreadPool(Math.max(maxThreads, 1));
        this.pckAudioFiles = this.game.getAudioFiles();
        this.wemAudioFiles = new ArrayList<>();

        // If an output dir is selected, use it. Otherwise return the current directory
        if (!outputDir.getName().equals("")) {
            this.outputDir = new File(outputDir, "extracted");
        } else {
            this.outputDir = new File(".", "extracted");
        }
    }

    public File getWorkspace() {

        return Utils.asLocalDirectory("workspace", this.game.getShortName());
    }

    public File getUnpackingOutput() {

        return Utils.asLocalDirectory(this.outputDir, this.game.getShortName());
    }

    private <T extends AudioFile> void run(String name, File output, AudioConverter<T> converter, Collection<T> files) {

        try (ProgressBar pb = Utils.defaultProgressBar(name, files.size())) {

            CompletableFuture<?>[] futures = files.stream()
                                                  .map(audio -> (Runnable) () -> {
                                                      try {
                                                          pb.setExtraMessage(audio.getName());
                                                          File out = converter.handle(audio, output);
                                                          audio.onHandled(out);
                                                          pb.step();
                                                      } catch (Exception e) {
                                                          throw new RuntimeException(e);
                                                      }
                                                  }).map(runnable -> CompletableFuture.runAsync(runnable, this.service))
                                                  .toArray(CompletableFuture[]::new);

            CompletableFuture.allOf(futures).join();
            pb.setExtraMessage("OK");
        }

        this.pckAudioFiles
                .stream()
                .flatMap(PckAudioFile::getOutputFiles)
                .forEach(this.wemAudioFiles::add);
    }


    public void unpackFiles() {

        this.run(" Unpacking", this.getWorkspace(), new Pck2Wem(), this.pckAudioFiles);
    }

    public void convertFiles() {

        this.run("Extracting", this.getUnpackingOutput(), new Wem2Wav(), this.wemAudioFiles);
    }
}
