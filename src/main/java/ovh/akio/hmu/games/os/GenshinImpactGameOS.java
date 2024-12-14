package ovh.akio.hmu.games.os;

import ovh.akio.hmu.games.cn.GenshinImpactGameCN;

import java.io.File;

public class GenshinImpactGameOS extends GenshinImpactGameCN {

    public GenshinImpactGameOS(File basePath) {

        super(basePath);
    }

    @Override
    public String getName() {

        return "Genshin Impact (OS)";
    }

    @Override
    public File getExecutableFile() {

        return new File(this.getBasePath(), "GenshinImpact.exe");
    }

    @Override
    public File getAudioDirectory() {

        return new File(this.getBasePath(), "GenshinImpact_Data\\StreamingAssets\\AudioAssets");
    }

}
