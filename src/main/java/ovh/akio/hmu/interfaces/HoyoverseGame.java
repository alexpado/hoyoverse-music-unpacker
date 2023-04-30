package ovh.akio.hmu.interfaces;

import ovh.akio.hmu.entities.PckAudioFile;
import ovh.akio.hmu.entities.UpdatePackage;
import ovh.akio.hmu.exceptions.InvalidGameDirectoryException;
import ovh.akio.hmu.games.GenshinImpactGame;
import ovh.akio.hmu.games.HonkaiImpactGame;
import ovh.akio.hmu.games.HonkaiStarRail;

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

    String getShortName();

    File getGameDirectory();

    File getAudioDirectory();

    UpdatePackage getUpdatePackage();

    List<PckAudioFile> getAudioFiles();
}
