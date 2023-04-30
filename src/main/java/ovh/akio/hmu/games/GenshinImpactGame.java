package ovh.akio.hmu.games;

import ovh.akio.hmu.Utils;
import ovh.akio.hmu.entities.PckAudioFile;
import ovh.akio.hmu.entities.UpdatePackage;
import ovh.akio.hmu.exceptions.InvalidGameDirectoryException;
import ovh.akio.hmu.interfaces.HoyoverseGame;

import java.io.File;
import java.util.List;

public class GenshinImpactGame implements HoyoverseGame {

    private final File          basePath;
    private final UpdatePackage updatePackage;

    public GenshinImpactGame(File basePath) {

        this.basePath = basePath;
        File gameDirectory = this.getGameDirectory();

        File gameExecutable = new File(gameDirectory, "GenshinImpact.exe");

        if (!gameExecutable.exists()) {
            throw new InvalidGameDirectoryException("The path provided does not point to a valid game directory.");
        }

        List<File> gameFiles = Utils.getDirectoryContent(gameDirectory);

        this.updatePackage = gameFiles.stream()
                                      .filter(UpdatePackage::isUpdatePackage)
                                      .findFirst()
                                      .map(UpdatePackage::new)
                                      .orElse(null);
    }

    @Override
    public File getGameDirectory() {

        return new File(this.basePath, "Genshin Impact game");
    }

    @Override
    public File getAudioDirectory() {

        return new File(this.getGameDirectory(), "GenshinImpact_Data\\StreamingAssets\\AudioAssets");
    }

    @Override
    public UpdatePackage getUpdatePackage() {

        return this.updatePackage;
    }

    @Override
    public String getName() {

        return "Genshin Impact";
    }

    @Override
    public List<PckAudioFile> getAudioFiles() {

        return Utils.scanPck(this.getAudioDirectory());
    }
}
