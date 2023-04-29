package ovh.akio.hmu.games;

import ovh.akio.hmu.entities.PckAudioFile;
import ovh.akio.hmu.entities.UpdatePackage;

import java.io.File;
import java.util.List;

public interface HoyoverseGame {

    String getName();

    File getGameDirectory();

    File getAudioDirectory();

    UpdatePackage getUpdatePackage();

    List<PckAudioFile> getAudioFiles();
}
