package ovh.akio.hmu.games.os;

import ovh.akio.hmu.games.cn.HonkaiStarRailCN;

import java.io.File;

public class HonkaiStarRailOS extends HonkaiStarRailCN {


    public HonkaiStarRailOS(File basePath) {

        super(basePath);
    }

    @Override
    public String getName() {

        return "Honkai Star Rail (OS)";
    }

    @Override
    public File getGameDirectory() {

        return new File(this.getBasePath(), "Games");
    }

}
