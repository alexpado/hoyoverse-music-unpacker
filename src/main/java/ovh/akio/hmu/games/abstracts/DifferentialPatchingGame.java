package ovh.akio.hmu.games.abstracts;

import me.tongfei.progressbar.ProgressBar;
import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import ovh.akio.hmu.DiskUtils;
import ovh.akio.hmu.Utils;
import ovh.akio.hmu.entities.PckAudioFile;
import ovh.akio.hmu.interfaces.HoyoverseGame;
import ovh.akio.hmu.interfaces.states.Patchable;
import ovh.akio.hmu.wrappers.HDiffPatchWrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

public abstract class DifferentialPatchingGame implements HoyoverseGame, Patchable {

    private final List<PckAudioFile> patchedPckAudioFiles;
    private final List<File>         updatePackageFiles;

    public DifferentialPatchingGame() {

        this.patchedPckAudioFiles = new ArrayList<>();
        this.updatePackageFiles = new ArrayList<>();
    }

    public abstract boolean mayHandleArchiveItem(ISimpleInArchiveItem archiveItem) throws SevenZipException;

    /**
     * Patch all files using {@link #getUpdateFiles()}.
     */
    @Override
    public void patch() throws IOException, InterruptedException {
        // Patching needed, hdiff files
        Map<PckAudioFile, File> filePatchMap = new HashMap<>();

        for (File hdiffFile : this.getUpdateFiles()) {
            String hdiffName = hdiffFile.getName().replaceAll(".pck.hdiff", "");
            for (PckAudioFile gamePckAudioFile : this.getAudioFiles()) {
                if (gamePckAudioFile.getName().equals(hdiffName)) {
                    filePatchMap.put(gamePckAudioFile, hdiffFile);
                }
            }
        }

        // It's patching time !
        HDiffPatchWrapper hdiffPatcher = new HDiffPatchWrapper();
        this.patchedPckAudioFiles.clear();

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
     * Extract the update package.
     */
    @Override
    public void extractUpdatePackage() throws IOException {


        Optional<File> optionalFile = this.getUpdatePackageFile();

        if (optionalFile.isEmpty()) {
            throw new IllegalStateException("No update package available");
        }

        this.updatePackageFiles.clear();

        try (ProgressBar pb = Utils.defaultProgressBar("Update Package", -1)) {
            try (RandomAccessFile raf = new RandomAccessFile(optionalFile.get(), "r")) {
                Map<Integer, ISimpleInArchiveItem> archiveMap = new HashMap<>();
                Map<Integer, File>                 fileMap    = new HashMap<>();

                File output = DiskUtils.update(this).toFile();
                output.mkdirs();

                IInArchive archive = SevenZip.openInArchive(null, new RandomAccessFileInStream(raf));

                for (ISimpleInArchiveItem archiveItem : archive.getSimpleInterface().getArchiveItems()) {

                    if (this.mayHandleArchiveItem(archiveItem)) {

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

        return this.updatePackageFiles;
    }
}
