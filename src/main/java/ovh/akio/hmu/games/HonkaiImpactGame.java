package ovh.akio.hmu.games;

import ovh.akio.hmu.enums.Game;

import java.io.File;

public class HonkaiImpactGame extends AbstractGame {

    public HonkaiImpactGame(File basePath) {

        super(basePath);
    }

    @Override
    public Game getGameType() {

        return Game.HI3;
    }

    @Override
    public String getName() {

        return "Honkai Impact 3rd (CN)";
    }

    @Override
    public String getShortName() {

        return "hi3";
    }

    @Override
    public File getExecutableFile() {

        return new File(this.getBasePath(), "BH3.exe");
    }

    @Override
    public File getAudioDirectory() {

        return new File(this.getBasePath(), "BH3_Data\\StreamingAssets\\Audio\\GeneratedSoundBanks\\Windows");
    }

}
