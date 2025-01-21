package ovh.akio.hmu.interfaces;

import ovh.akio.hmu.entities.PckAudioFile;
import ovh.akio.hmu.enums.Game;
import ovh.akio.hmu.exceptions.InvalidGameDirectoryException;
import ovh.akio.hmu.games.cn.GenshinImpactGameCN;
import ovh.akio.hmu.games.cn.HonkaiImpactGameCN;
import ovh.akio.hmu.games.cn.HonkaiStarRailCN;
import ovh.akio.hmu.games.os.GenshinImpactGameOS;
import ovh.akio.hmu.games.os.HonkaiImpactGameOS;
import ovh.akio.hmu.games.os.HonkaiStarRailOS;

import java.io.File;
import java.util.List;
import java.util.function.Predicate;

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

    Game getGameType();

    String getName();

    String getShortName();

    File getBasePath();

    File getExecutableFile();

    File getAudioDirectory();

    List<PckAudioFile> getAudioFiles(Predicate<File> filter);

}
