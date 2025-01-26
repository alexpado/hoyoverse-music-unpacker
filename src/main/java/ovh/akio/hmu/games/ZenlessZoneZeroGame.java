package ovh.akio.hmu.games;

import ovh.akio.hmu.enums.Game;

import java.io.File;

public class ZenlessZoneZeroGame extends AbstractGame {


    public ZenlessZoneZeroGame(File basePath) {

        super(basePath);
    }

    @Override
    public Game getGameType() {

        return Game.ZZZ;
    }

    @Override
    public String getName() {

        return "Zenless Zone Zero (CN)";
    }

    @Override
    public String getShortName() {

        return "zzz";
    }

    @Override
    public File getExecutableFile() {

        return new File(this.getBasePath(), "ZenlessZoneZero.exe");
    }

    @Override
    public File getAudioDirectory() {

        return new File(this.getBasePath(), "ZenlessZoneZero_Data\\StreamingAssets\\Audio\\Windows");
    }

}
