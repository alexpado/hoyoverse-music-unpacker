package ovh.akio.hmu.games.cn;

import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import ovh.akio.hmu.Utils;
import ovh.akio.hmu.entities.PckAudioFile;
import ovh.akio.hmu.exceptions.InvalidGameDirectoryException;
import ovh.akio.hmu.games.abstracts.DifferentialPatchingGame;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HonkaiStarRailCN extends DifferentialPatchingGame {

    private final File               basePath;
    private final File               updatePackage;
    private       List<PckAudioFile> gamePckAudioFiles = null;

    public HonkaiStarRailCN(File basePath) {

        this.basePath = basePath;

        File gameExecutable = new File(this.getBasePath(), "StarRail.exe");

        if (!gameExecutable.exists()) {
            throw new InvalidGameDirectoryException("The path provided does not point to a valid game directory.");
        }

        List<File> gameFiles = Utils.getDirectoryContent(this.getBasePath());
        Pattern    pattern   = Pattern.compile("game_(?<in>.*)_(?<out>.*)_hdiff_(?<sign>.*)\\.zip");

        this.updatePackage = gameFiles.stream()
                                      .filter(file -> {
                                          Matcher matcher = pattern.matcher(file.getName());
                                          return matcher.matches();
                                      })
                                      .findFirst().orElse(null);
    }

    @Override
    public boolean mayHandleArchiveItem(ISimpleInArchiveItem archiveItem) throws SevenZipException {

        boolean isPckDiff = archiveItem.getPath().endsWith(".pck.hdiff");
        boolean isMinimum = archiveItem.getPath().contains("Minimum");
        boolean isMusic   = archiveItem.getPath().contains("Music");

        return isPckDiff && (isMinimum || isMusic);
    }

    @Override
    public String getName() {

        return "Honkai Star Rail (CN)";
    }

    @Override
    public String getShortName() {

        return "hsr";
    }

    public File getBasePath() {

        return this.basePath;
    }

    @Override
    public File getExecutableFile() {

        return new File(this.getBasePath(), "StarRail.exe");
    }

    @Override
    public File getAudioDirectory() {

        return new File(this.getBasePath(), "StarRail_Data\\StreamingAssets\\Audio\\AudioPackage\\Windows");
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
     * Retrieve an {@link Optional} {@link File} representing the update package.
     *
     * @return An {@link Optional} {@link File}.
     */
    @Override
    public Optional<File> getUpdatePackageFile() {

        return Optional.ofNullable(this.updatePackage);
    }

}
