package ovh.akio.hmu;

import me.tongfei.progressbar.ProgressBar;
import ovh.akio.hmu.entities.PckAudioFile;
import ovh.akio.hmu.entities.UpdatePackage;
import ovh.akio.hmu.entities.WemAudioFile;
import ovh.akio.hmu.interfaces.AudioConverter;
import ovh.akio.hmu.interfaces.AudioFile;
import ovh.akio.hmu.interfaces.HoyoverseGame;
import ovh.akio.hmu.wrappers.HDiffPatchWrapper;
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
        version = "Hoyoverse Audio Extractor 1.2"
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

        System.out.println("Detecting game...");
        HoyoverseGame game = HoyoverseGame.of(this.gameFolder);
        System.out.println("  -- Detected " + game.getName() + " !");

        if (!this.diffMode && game.getUpdatePackage() != null) {
            System.out.println("An update package has been detected. To run the extraction in diff mode, add the --diff flag.");
        }

        if (this.diffMode && game.getUpdatePackage() == null) {
            System.err.println("No update package was detected but the --diff flag was enabled. Please try again without the --diff flag.");
            return 1;
        }

        GameUnpacker unpacker = new GameUnpacker(game, this.threadCount, outputFolder);

        // Cleaning up the previous runs
        System.out.println("Removing previous files...");
        Utils.delete(unpacker.getWorkspace());
        Utils.delete(unpacker.getUnpackingOutput());

        if (!this.diffMode) {
            System.out.println("Starting unpacking...");
            unpacker.unpackFiles();
            unpacker.convertFiles();
            return 0;
        }

        // TODO:
        //  If in DIFF mode:
        //   1. Unpack PCK files to workspace (/wem)
        //   2. Convert WEM files to WAV in workspace (/wav)
        //   3. Unzip update zip package to workspace (/patch)
        //   4. Patch PCK files using update package (/pck-patched)
        //   5. Unpack patched PCK files (/wem-patched)
        //   6. Convert WEM to WAV (/wav-patched)
        //   7. Index files in /wav & /wav-patched
        //   8. Compare/delete files and rename if --diff

        // TODO: Refactor the code below when an update package will be available.
        /*
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
        }*/

        return 0;
    }
}
