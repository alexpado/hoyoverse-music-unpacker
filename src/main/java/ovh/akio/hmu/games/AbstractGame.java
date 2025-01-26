package ovh.akio.hmu.games;

import ovh.akio.hmu.AppUtils;
import ovh.akio.hmu.entities.PckAudioFile;
import ovh.akio.hmu.enums.ListingOption;
import ovh.akio.hmu.exceptions.InvalidGameDirectoryException;
import ovh.akio.hmu.interfaces.HoyoverseGame;

import java.io.File;
import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractGame implements HoyoverseGame {

    private final File               basePath;
    private       List<PckAudioFile> audioFiles = null;

    public AbstractGame(File basePath) {

        this.basePath = basePath;

        if (!this.getExecutableFile().exists()) {
            throw new InvalidGameDirectoryException("The path provided does not point to a valid game directory.");
        }
    }

    @Override
    public File getBasePath() {

        return this.basePath;
    }

    @Override
    public List<PckAudioFile> getAudioFiles(Predicate<File> filter) {

        if (this.audioFiles != null) {
            return this.audioFiles;
        }

        this.audioFiles = AppUtils.getDirectoryContents(this.getAudioDirectory(), ListingOption.RECURSIVE)
                                  .stream()
                                  .filter(file -> file.getName().endsWith(".pck"))
                                  .filter(filter)
                                  .map(PckAudioFile::new)
                                  .toList();

        return this.audioFiles;
    }

}
