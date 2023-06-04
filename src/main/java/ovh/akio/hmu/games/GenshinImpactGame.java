package ovh.akio.hmu.games;

import me.tongfei.progressbar.ProgressBar;
import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import ovh.akio.hmu.DiskUtils;
import ovh.akio.hmu.Utils;
import ovh.akio.hmu.entities.PckAudioFile;
import ovh.akio.hmu.exceptions.InvalidGameDirectoryException;
import ovh.akio.hmu.interfaces.HoyoverseGame;
import ovh.akio.hmu.interfaces.states.Patchable;
import ovh.akio.hmu.wrappers.HDiffPatchWrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenshinImpactGame implements HoyoverseGame, Patchable {

    private final File               basePath;
    private final File               updatePackage;
    private       List<PckAudioFile> gamePckAudioFiles    = null;
    private       List<PckAudioFile> patchedPckAudioFiles = null;
    private       List<File>         updatePackageFiles   = null;

    public GenshinImpactGame(File basePath) {

        this.basePath = basePath;
        File gameDirectory = this.getGameDirectory();

        File gameExecutable = new File(gameDirectory, "GenshinImpact.exe");

        if (!gameExecutable.exists()) {
            throw new InvalidGameDirectoryException("The path provided does not point to a valid game directory.");
        }

        List<File> gameFiles = Utils.getDirectoryContent(this.getGameDirectory());
        Pattern    pattern   = Pattern.compile("game_(?<in>.*)_(?<out>.*)_hdiff_(?<sign>.*)\\.zip");

        this.updatePackage = gameFiles.stream()
                                      .filter(file -> {
                                          Matcher matcher = pattern.matcher(file.getName());
                                          return matcher.matches();
                                      })
                                      .findFirst().orElse(null);
    }

    @Override
    public File getGameDirectory() {

        return new File(this.basePath, "Genshin Impact game");
    }

    @Override
    public File getAudioDirectory() {

        return new File(this.getGameDirectory(), "GenshinImpact_Data\\StreamingAssets\\AudioAssets");
    }

    @Override
    public String getName() {

        return "Genshin Impact";
    }

    @Override
    public String getShortName() {

        return "gi";
    }

    @Override
    public List<PckAudioFile> getAudioFiles() {

        if (this.gamePckAudioFiles != null) {
            return this.gamePckAudioFiles;
        }

        this.gamePckAudioFiles = Utils.scanPck(this.getAudioDirectory());
        return this.gamePckAudioFiles;
    }

    /**
     * Patch all files using {@link #getUpdateFiles()}.
     */
    @Override
    public void patch() throws IOException, InterruptedException {

        // Patching needed, hdiff files
        Map<PckAudioFile, File> filePatchMap = new HashMap<>();

        for (File hdiffFile : this.updatePackageFiles) {
            String hdiffName = hdiffFile.getName().replaceAll(".pck.hdiff", "");
            for (PckAudioFile gamePckAudioFile : this.gamePckAudioFiles) {
                if (gamePckAudioFile.getName().equals(hdiffName)) {
                    filePatchMap.put(gamePckAudioFile, hdiffFile);
                }
            }
        }

        // It's patching time !
        HDiffPatchWrapper hdiffPatcher = new HDiffPatchWrapper();
        this.patchedPckAudioFiles = new ArrayList<>();


        try (ProgressBar pb = Utils.defaultProgressBar("Patching", filePatchMap.size())) {
            for (PckAudioFile originalFile : filePatchMap.keySet()) {
                pb.setExtraMessage(originalFile.getName());
                File patchingFile = filePatchMap.get(originalFile);
                File patchedFile  = new File(patchingFile.getParentFile(), originalFile.getName() + ".pck");

                PckAudioFile patched = hdiffPatcher.patch(originalFile, patchingFile, patchedFile);
                this.patchedPckAudioFiles.add(patched);

                pb.step();
            }
            pb.setExtraMessage("OK");
        }
    }

    /**
     * Retrieve all patched {@link PckAudioFile}.
     *
     * @return A {@link List} of {@link File}.
     */
    @Override
    public List<PckAudioFile> getPatchedFiles() {

        return this.patchedPckAudioFiles;
    }

    /**
     * Retrieve an {@link Optional} {@link File} representing the update package.
     *
     * @return An {@link Optional} {@link File}.
     */
    @Override
    public Optional<File> getUpdatePackageFile() {

        return Optional.ofNullable(this.updatePackage);
    }

    /**
     * Extract the update package.
     */
    @Override
    public void extractUpdatePackage() throws IOException {


        Optional<File> optionalFile = this.getUpdatePackageFile();

        if (optionalFile.isEmpty()) {
            throw new IllegalStateException("No update package available");
        }

        this.updatePackageFiles = new ArrayList<>();

        try (ProgressBar pb = Utils.defaultProgressBar("Update Package", -1)) {
            try (RandomAccessFile raf = new RandomAccessFile(optionalFile.get(), "r")) {
                Map<Integer, ISimpleInArchiveItem> archiveMap = new HashMap<>();
                Map<Integer, File>                 fileMap    = new HashMap<>();

                File output = DiskUtils.update(this).toFile();
                output.mkdirs();

                IInArchive archive = SevenZip.openInArchive(null, new RandomAccessFileInStream(raf));

                for (ISimpleInArchiveItem archiveItem : archive.getSimpleInterface().getArchiveItems()) {

                    boolean isPckDiff = archiveItem.getPath().endsWith(".pck.hdiff");
                    boolean isMinimum = archiveItem.getPath().contains("Minimum");
                    boolean isMusic   = archiveItem.getPath().contains("Music");

                    if (isPckDiff && (isMinimum || isMusic)) {

                        archiveMap.put(archiveItem.getItemIndex(), archiveItem);

                        File outputFile = new File(output, archiveItem.getPath()
                                                                      .substring(archiveItem.getPath()
                                                                                            .lastIndexOf("\\") + 1));
                        if (outputFile.exists()) {
                            outputFile.delete();
                            outputFile.createNewFile();
                        }

                        fileMap.put(archiveItem.getItemIndex(), outputFile);
                        this.updatePackageFiles.add(outputFile);
                    }
                }


                int[] indices = new int[archiveMap.size()];

                List<Integer> list = archiveMap.keySet().stream().toList();
                for (int i = 0 ; i < list.size() ; i++) {
                    indices[i] = list.get(i);
                }

                archive.extract(indices, false, new IArchiveExtractCallback() {

                    @Override
                    public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode) {

                        if (extractAskMode != ExtractAskMode.EXTRACT) {
                            return null;
                        }

                        return data -> {
                            File outputFile = fileMap.get(index);
                            try {
                                try (FileOutputStream stream = new FileOutputStream(outputFile, true)) {
                                    stream.write(data);
                                    return data.length;
                                }
                            } catch (Exception e) {
                                throw new SevenZipException(e);
                            }
                        };
                    }

                    @Override
                    public void prepareOperation(ExtractAskMode extractAskMode) {}

                    @Override
                    public void setOperationResult(ExtractOperationResult extractOperationResult) {}

                    @Override
                    public void setTotal(long total) {

                        pb.maxHint(total / (1024 * 1024));
                    }

                    @Override
                    public void setCompleted(long complete) {

                        pb.stepTo(complete / (1024 * 1024));
                    }
                });
            }
        }
    }

    /**
     * Retrieve all files from the update package.
     *
     * @return A {@link List} of {@link File}
     */
    @Override
    public List<File> getUpdateFiles() {

        return null;
    }
}
