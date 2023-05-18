package ovh.akio.hmu;

import ovh.akio.hmu.interfaces.HoyoverseGame;

import java.io.File;
import java.nio.file.Path;

public class DiskUtils {


    public static Path workspace(HoyoverseGame game) {

        return workspace(game, new File("."));
    }

    public static Path workspace(HoyoverseGame game, File root) {

        return Path.of(root.getAbsolutePath(), "workspace", game.getShortName());
    }

    public static Path unpack(HoyoverseGame game) {

        return Path.of(workspace(game).toString(), "unpacked");
    }

    public static Path unpack(HoyoverseGame game, File root) {

        return Path.of(workspace(game, root).toString(), "unpacked");
    }

    public static Path extracted(HoyoverseGame game) {

        return extracted(game, new File(".", "extracted"));
    }

    public static Path extracted(HoyoverseGame game, File root) {

        return Path.of(root.getAbsolutePath(), game.getShortName());
    }

    public static Path update(HoyoverseGame game) {

        return Path.of(workspace(game).toString(), "update-package");
    }

    public static Path update(HoyoverseGame game, File root) {

        return Path.of(workspace(game, root).toString(), "update-package");
    }

    public static Path unpackUpdate(HoyoverseGame game) {

        return Path.of(workspace(game).toString(), "updated");
    }

    public static Path unpackUpdate(HoyoverseGame game, File root) {

        return Path.of(workspace(game, root).toString(), "updated");
    }


}
