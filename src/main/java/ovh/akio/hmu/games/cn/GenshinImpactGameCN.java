package ovh.akio.hmu.games.cn;

import ovh.akio.hmu.enums.Game;
import ovh.akio.hmu.games.AbstractGame;

import java.io.File;

public class GenshinImpactGameCN extends AbstractGame {

    public GenshinImpactGameCN(File basePath) {

        super(basePath);
    }

    @Override
    public Game getGameType() {

        return Game.GI;
    }

    @Override
    public String getName() {

        return "Genshin Impact (CN)";
    }

    @Override
    public String getShortName() {

        return "gi";
    }


    @Override
    public File getExecutableFile() {

        return new File(this.getBasePath(), "YuanShen.exe");
    }

    @Override
    public File getAudioDirectory() {

        return new File(this.getBasePath(), "YuanShen_Data\\StreamingAssets\\AudioAssets");
    }

}
