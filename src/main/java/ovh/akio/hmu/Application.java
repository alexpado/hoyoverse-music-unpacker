package ovh.akio.hmu;

import me.tongfei.progressbar.ProgressBar;
import ovh.akio.hmu.entities.PckAudioFile;
import ovh.akio.hmu.entities.UpdatePackage;
import ovh.akio.hmu.entities.WemAudioFile;
import ovh.akio.hmu.exceptions.InvalidGameDirectoryException;
import ovh.akio.hmu.games.GenshinImpactGame;
import ovh.akio.hmu.games.HonkaiStarRail;
import ovh.akio.hmu.games.HoyoverseGame;
import ovh.akio.hmu.interfaces.AudioConverter;
import ovh.akio.hmu.interfaces.AudioFile;
import ovh.akio.hmu.wrappers.HDiffPatchWrapper;
import ovh.akio.hmu.wrappers.Pck2Wem;
import ovh.akio.hmu.wrappers.Wem2Wav;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@CommandLine.Command(
        name = "extract",
        mixinStandardHelpOptions = true,
        version = "Hoyoverse Audio Extractor 1.1"
)
public class Application implements Callable<Integer> {

    private static final File WORKSPACE           = new File(".", "workspace");
    private static final File EXTRACT_PCK_WEM_OUT = new File(WORKSPACE, "out\\wem");
    private static final File EXTRACT_WEM_WAV_OUT = new File(WORKSPACE, "out\\wav");
    private static final File UPDATE_PCK_SRC_OUT  = new File(WORKSPACE, "update\\pck");
    private static final File UPDATE_PCK_WEM_OUT  = new File(WORKSPACE, "update\\wem");
    private static final File UPDATE_WEM_WAV_OUT  = new File(WORKSPACE, "update\\wav");
    private static final File UPDATE_DIFF_OUT     = new File(WORKSPACE, "update\\patched");
    private static final File UPDATE_DIFF_SRC     = new File(WORKSPACE, "update\\diff");

    @CommandLine.Option(
            names = {"-g", "--game"},
            description = "Installation folder of the game",
            required = true
    )
    private File gameFolder;

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

    public static void main(String... args) {

        System.exit(new CommandLine(new Application()).execute(args));
    }

    private static File newFile(ZipEntry zipEntry) throws IOException {

        String name     = zipEntry.getName().substring(zipEntry.getName().lastIndexOf("/"));
        File   destFile = new File(Application.UPDATE_DIFF_SRC, name);

        String destDirPath  = Application.UPDATE_DIFF_SRC.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
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

        if (WORKSPACE.exists()) {
            Utils.delete(WORKSPACE);
        }

        EXTRACT_PCK_WEM_OUT.mkdirs();
        EXTRACT_WEM_WAV_OUT.mkdirs();

        AudioConverter<PckAudioFile> unpacker  = new Pck2Wem();
        AudioConverter<WemAudioFile> converter = new Wem2Wav();

        System.out.println("Detecting game...");
        HoyoverseGame game = HoyoverseGame.of(this.gameFolder);
        System.out.println("Detected " + game.getName() + " !");

        System.out.println("  > Audio Files detected: " + game.getAudioFiles().size());

        if (!this.diffMode && game.getUpdatePackage() != null) {
            System.out.println("An update package has been detected. To run the extraction in diff mode, add the --diff flag.");
        }

        if (this.diffMode && game.getUpdatePackage() == null) {
            System.err.println("No update package was detected but the --diff flag was enabled. Please try again without the --diff flag.");
            return 1;
        }

        List<PckAudioFile> pckFiles        = game.getAudioFiles();
        List<File>         currentWavFiles = this.runExtract(unpacker, converter, pckFiles, EXTRACT_PCK_WEM_OUT, EXTRACT_WEM_WAV_OUT);

        if (this.diffMode && game.getUpdatePackage() != null) {

            UPDATE_PCK_SRC_OUT.mkdirs();
            UPDATE_PCK_WEM_OUT.mkdirs();
            UPDATE_WEM_WAV_OUT.mkdirs();
            UPDATE_DIFF_OUT.mkdirs();
            UPDATE_DIFF_SRC.mkdirs();

            try (ProgressBar p = Utils.defaultProgressBar("Extracting", 1)) {
                UpdatePackage updatePackage = game.getUpdatePackage();
                this.extractUpdatePackage(updatePackage);
                p.setExtraMessage("OK");
                p.step();
            }

            HDiffPatchWrapper  patcher         = new HDiffPatchWrapper();
            List<PckAudioFile> patchedPckFiles = new ArrayList<>();

            try (ProgressBar p = Utils.defaultProgressBar("  Patching", pckFiles.size())) {
                for (PckAudioFile pckFile : pckFiles) {
                    File source = pckFile.getSource();
                    p.setExtraMessage(source.getName());
                    File target = new File(UPDATE_PCK_SRC_OUT, source.getName());
                    Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    File diffFile = new File(UPDATE_DIFF_SRC, source.getName() + ".hdiff");
                    File patched  = new File(UPDATE_DIFF_OUT, source.getName());

                    if (diffFile.exists()) {
                        patchedPckFiles.add(patcher.patch(target, diffFile, patched));
                    }
                    p.step();
                }
                p.setExtraMessage("OK");
            }

            List<File> newWaveFiles = this.runExtract(unpacker, converter, patchedPckFiles, UPDATE_DIFF_OUT, UPDATE_WEM_WAV_OUT);
            pckFiles.clear();
            patchedPckFiles.clear();

            Map<String, String> hashMap = new HashMap<>();

            try (ProgressBar p = Utils.defaultProgressBar("  Indexing", currentWavFiles.size())) {
                for (File currentWavFile : currentWavFiles) {
                    p.setExtraMessage(currentWavFile.getName());
                    hashMap.put(currentWavFile.getName(), Utils.hashFile(currentWavFile));
                    p.step();
                }
                p.setExtraMessage("OK");
            }

            try (ProgressBar p = Utils.defaultProgressBar(" Comparing", newWaveFiles.size())) {
                for (File newWaveFile : newWaveFiles) {
                    p.setExtraMessage(newWaveFile.getName());
                    String name = newWaveFile.getName();
                    String hash = Utils.hashFile(newWaveFile);

                    FileFlag flag = FileFlag.CREATED;
                    if (hashMap.containsKey(name)) {
                        String currentHash = hashMap.get(name);
                        if (currentHash.equals(hash)) {
                            flag = FileFlag.DUPLICATED;
                        } else {
                            flag = FileFlag.UPDATED;
                        }
                    } else if (hashMap.containsValue(hash)) {
                        flag = FileFlag.POSSIBLY_DUPLICATED;
                    }

                    if (this.prefixEnabled && flag != FileFlag.DUPLICATED) {
                        flag.rename(newWaveFile);
                    } else if (flag == FileFlag.DUPLICATED) {
                        newWaveFile.delete();
                    }
                    p.step();
                }
                p.setExtraMessage("OK");
            }

            Utils.delete(EXTRACT_PCK_WEM_OUT);
            Utils.delete(EXTRACT_WEM_WAV_OUT);
            Utils.delete(UPDATE_PCK_SRC_OUT);
            Utils.delete(UPDATE_PCK_WEM_OUT);
            Utils.delete(UPDATE_DIFF_OUT);
            Utils.delete(UPDATE_DIFF_SRC);

            return 0;
        }

        Utils.delete(EXTRACT_PCK_WEM_OUT);
        return 0;
    }

    private List<File> runExtract(AudioConverter<PckAudioFile> unpacker, AudioConverter<WemAudioFile> converter, List<PckAudioFile> pckFiles, File pckOut, File wemOut) {

        this.runConvert(" Unpacking", pckOut, unpacker, pckFiles);
        List<WemAudioFile> wemFiles = pckFiles.stream().flatMap(PckAudioFile::getOutputFiles).toList();
        this.runConvert("Converting", wemOut, converter, wemFiles);
        return wemFiles.stream().map(WemAudioFile::getOutput).collect(Collectors.toList());
    }

    private void extractUpdatePackage(UpdatePackage updatePackage) throws Exception {


        byte[]         buffer   = new byte[1024];
        ZipInputStream zis      = new ZipInputStream(new FileInputStream(updatePackage.getUpdatePackage()));
        ZipEntry       zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            if (zipEntry.getName().contains("Windows/Minimum") || zipEntry.getName().contains("Windows/Music")) {
                File newFile = newFile(zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    // write file content
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int              len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }

    private <T extends AudioFile> void runConvert(String name, File root, AudioConverter<T> converter, List<T> files) {

        AtomicInteger   counter = new AtomicInteger(0);
        ExecutorService pool    = Executors.newFixedThreadPool(Math.max(this.threadCount, 1));
        List<Runnable>  tasks   = new ArrayList<>();

        try (ProgressBar p = Utils.defaultProgressBar(name, files.size())) {
            for (T file : files) {
                final T fFile = file;
                tasks.add(() -> {
                    try {
                        p.setExtraMessage(fFile.getName());
                        File output = converter.handle(fFile, root);
                        fFile.onHandled(output);
                        p.step();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            CompletableFuture<?>[] futures = tasks.stream()
                                                  .map(task -> CompletableFuture.runAsync(task, pool))
                                                  .toArray(CompletableFuture[]::new);

            CompletableFuture.allOf(futures).join();
            pool.shutdown();
            p.setExtraMessage("OK");
        }
    }

}
