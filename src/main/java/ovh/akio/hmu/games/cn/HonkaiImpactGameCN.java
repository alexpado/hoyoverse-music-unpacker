package ovh.akio.hmu.games.cn;

import me.tongfei.progressbar.ProgressBar;
import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import ovh.akio.hmu.DiskUtils;
import ovh.akio.hmu.Utils;
import ovh.akio.hmu.entities.PckAudioFile;
import ovh.akio.hmu.enums.Game;
import ovh.akio.hmu.exceptions.InvalidGameDirectoryException;
import ovh.akio.hmu.interfaces.HoyoverseGame;
import ovh.akio.hmu.interfaces.states.Patchable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HonkaiImpactGameCN implements HoyoverseGame, Patchable {

    private final File               basePath;
    private       File               updatePackage;
    private       List<PckAudioFile> gamePckAudioFiles    = null;
    private       List<PckAudioFile> patchedPckAudioFiles = null;
    private       List<File>         updatePackageFiles   = null;

    public HonkaiImpactGameCN(File basePath) {

        this.basePath = basePath;
        File gameExecutable = this.getExecutableFile();

        if (!gameExecutable.exists()) {
            throw new InvalidGameDirectoryException("The path provided does not point to a valid game directory.");
        }


        List<File> gameFiles = Utils.getDirectoryContent(this.getBasePath());
        Pattern    pattern   = Pattern.compile("BH3_v(?<out>.*)_(?<sign>.*)\\.7z");

        this.updatePackage = gameFiles.stream()
                                      .filter(file -> {
                                          Matcher matcher = pattern.matcher(file.getName());
                                          return matcher.matches();
                                      })
                                      .findFirst().orElse(null);
    }

    @Override
    public Game getGameType() {

        return Game.HI3;
    }

    @Override
    public String getName() {

        return "Honkai Impact 3rd (CN)";
    }

    @Override
    public String getShortName() {

        return "hi3";
    }

    @Override
    public File getBasePath() {

        return this.basePath;
    }

    @Override
    public File getExecutableFile() {

        return new File(this.getBasePath(), "BH3.exe");
    }

    @Override
    public File getAudioDirectory() {

        return new File(this.getBasePath(), "BH3_Data\\StreamingAssets\\Audio\\GeneratedSoundBanks\\Windows");
    }

    @Override
    public List<PckAudioFile> getAudioFiles(Predicate<File> filter) {

        if (this.gamePckAudioFiles != null) {
            return this.gamePckAudioFiles;
        }

        this.gamePckAudioFiles = Utils.scanPck(this.getAudioDirectory(), filter);
        return this.gamePckAudioFiles;
    }

    /**
     * Patch all files using {@link #getUpdateFiles()}.
     *
     * @param filter
     *         The {@link Predicate} to use when filtering files to patch.
     */
    @Override
    public void patch(Predicate<File> filter) {
        // No patching needed, no hdiff files for HI3
        this.patchedPckAudioFiles = this.updatePackageFiles.stream()
                                                           .filter(File::isFile)
                                                           .filter(file -> file.getName().endsWith(".pck"))
                                                           .filter(filter)
                                                           .map(PckAudioFile::new)
                                                           .toList();
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
            throw new IllegalStateException("No update package available.");
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
                    if (archiveItem.getPath().endsWith(".pck") && archiveItem.getPath().contains("_Default")) {

                        archiveMap.put(archiveItem.getItemIndex(), archiveItem);

                        File outputFile = new File(output,
                                                   archiveItem.getPath()
                                                              .substring(archiveItem.getPath().lastIndexOf("\\") + 1)
                        );
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
                for (int i = 0; i < list.size(); i++) {
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
                    public void setOperationResult(ExtractOperationResult extractOperationResult) throws SevenZipException {}

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

        return this.updatePackageFiles;
    }

}
