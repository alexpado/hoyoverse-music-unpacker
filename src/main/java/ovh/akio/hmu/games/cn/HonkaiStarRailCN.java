package ovh.akio.hmu.games.cn;

import ovh.akio.hmu.enums.Game;
import ovh.akio.hmu.games.AbstractGame;

import java.io.File;

public class HonkaiStarRailCN extends AbstractGame {


    public HonkaiStarRailCN(File basePath) {

        super(basePath);
    }

    @Override
    public Game getGameType() {

        return Game.HSR;
    }

    @Override
    public String getName() {

        return "Honkai Star Rail (CN)";
    }

    @Override
    public String getShortName() {

        return "hsr";
    }

    @Override
    public File getExecutableFile() {

        return new File(this.getBasePath(), "StarRail.exe");
    }

    @Override
    public File getAudioDirectory() {

        return new File(this.getBasePath(), "StarRail_Data\\StreamingAssets\\Audio\\AudioPackage\\Windows");
    }

}
