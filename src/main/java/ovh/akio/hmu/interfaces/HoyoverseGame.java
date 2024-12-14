package ovh.akio.hmu.interfaces;

import ovh.akio.hmu.entities.PckAudioFile;
import ovh.akio.hmu.exceptions.InvalidGameDirectoryException;
import ovh.akio.hmu.games.cn.*;
import ovh.akio.hmu.games.os.*;

import java.io.File;
import java.util.List;

public interface HoyoverseGame {

    static HoyoverseGame of(File path) {
        // Detect game
        try {
            return new GenshinImpactGameOS(path);
        } catch (InvalidGameDirectoryException ignore) {

        }

        try {
            return new HonkaiStarRailOS(path);
        } catch (InvalidGameDirectoryException ignore) {
        }


        try {
            return new HonkaiImpactGameOS(path);
        } catch (InvalidGameDirectoryException ignore) {
        }

        try {
            return new GenshinImpactGameCN(path);
        } catch (InvalidGameDirectoryException ignore) {

        }

        try {
            return new HonkaiStarRailCN(path);
        } catch (InvalidGameDirectoryException ignore) {
        }


        try {
            return new HonkaiImpactGameCN(path);
        } catch (InvalidGameDirectoryException ignore) {
        }


        throw new IllegalStateException("Could not detect to which game the provided path belongs");
    }

    String getName();

    String getShortName();

    File getBasePath();

    File getExecutableFile();

    File getAudioDirectory();

    List<PckAudioFile> getAudioFiles();

}
