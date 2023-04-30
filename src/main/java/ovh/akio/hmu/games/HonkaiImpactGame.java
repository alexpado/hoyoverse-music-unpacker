package ovh.akio.hmu.games;

import ovh.akio.hmu.Utils;
import ovh.akio.hmu.entities.PckAudioFile;
import ovh.akio.hmu.entities.UpdatePackage;
import ovh.akio.hmu.exceptions.InvalidGameDirectoryException;
import ovh.akio.hmu.interfaces.HoyoverseGame;

import java.io.File;
import java.util.List;

public class HonkaiImpactGame implements HoyoverseGame {

    private final File basePath;

    public HonkaiImpactGame(File basePath) {

        this.basePath = basePath;
        File gameDirectory = this.getGameDirectory();

        File gameExecutable = new File(gameDirectory, "BH3.exe");

        if (!gameExecutable.exists()) {
            throw new InvalidGameDirectoryException("The path provided does not point to a valid game directory.");
        }
    }

    @Override
    public String getName() {

        return "Honkai Impact 3rd";
    }

    @Override
    public String getShortName() {

        return "hi3";
    }

    @Override
    public File getGameDirectory() {

        return new File(this.basePath, "Games");
    }


    @Override
    public File getAudioDirectory() {

        return new File(this.getGameDirectory(), "BH3_Data\\StreamingAssets\\Audio\\GeneratedSoundBanks\\Windows");
    }

    @Override
    public UpdatePackage getUpdatePackage() {

        return null;
    }

    @Override
    public List<PckAudioFile> getAudioFiles() {

        return Utils.getDirectoryContent(this.getAudioDirectory())
                    .stream()
                    .filter(File::isFile)
                    .filter(file -> file.getName().endsWith(".pck"))
                    .filter(file -> file.getName().contains("_Default"))
                    .map(PckAudioFile::new)
                    .toList();
    }
}
