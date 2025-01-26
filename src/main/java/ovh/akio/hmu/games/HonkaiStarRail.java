package ovh.akio.hmu.games;

import ovh.akio.hmu.enums.Game;

import java.io.File;

public class HonkaiStarRail extends AbstractGame {


    public HonkaiStarRail(File basePath) {

        super(basePath);
    }

    @Override
    public Game getGameType() {

        return Game.HSR;
    }

    @Override
    public String getName() {

        return "Honkai Star Rail";
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
