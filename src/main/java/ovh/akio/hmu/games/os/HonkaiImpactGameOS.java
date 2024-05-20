package ovh.akio.hmu.games.os;

import ovh.akio.hmu.games.cn.HonkaiImpactGameCN;

import java.io.File;

public class HonkaiImpactGameOS extends HonkaiImpactGameCN {

    public HonkaiImpactGameOS(File basePath) {

        super(basePath);
    }

    @Override
    public String getName() {

        return "Honkai Impact 3rd (OS)";
    }

    @Override
    public File getGameDirectory() {

        return new File(this.getBasePath(), "Games");
    }

}
