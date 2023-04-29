package ovh.akio.genshin.games;

import ovh.akio.genshin.entities.PckAudioFile;
import ovh.akio.genshin.entities.UpdatePackage;

import java.io.File;
import java.util.List;

public interface HoyoverseGame {

    String getName();

    File getGameDirectory();

    File getAudioDirectory();

    UpdatePackage getUpdatePackage();

    List<PckAudioFile> getAudioFiles();
}
