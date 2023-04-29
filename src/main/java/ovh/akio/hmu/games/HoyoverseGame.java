package ovh.akio.hmu.games;

import ovh.akio.hmu.entities.PckAudioFile;
import ovh.akio.hmu.entities.UpdatePackage;
import ovh.akio.hmu.exceptions.InvalidGameDirectoryException;

import java.io.File;
import java.util.List;

public interface HoyoverseGame {

    static HoyoverseGame of(File path) {
        // Detect game
        try {
            return new GenshinImpactGame(path);
        } catch (InvalidGameDirectoryException ignore) {

        }

        try {
            return new HonkaiStarRail(path);
        } catch (InvalidGameDirectoryException ignore) {}


        try {
            return new HonkaiImpactGame(path);
        } catch (InvalidGameDirectoryException ignore) {}

        throw new IllegalStateException("Could not detect to which game the provided path belongs");
    }

    String getName();

    File getGameDirectory();

    File getAudioDirectory();

    UpdatePackage getUpdatePackage();

    List<PckAudioFile> getAudioFiles();
}
