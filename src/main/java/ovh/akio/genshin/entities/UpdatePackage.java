package ovh.akio.genshin.entities;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdatePackage {

    private final File   updatePackage;
    private final String fromVersion;
    private final String toVersion;
    private final String signature;
    public UpdatePackage(File updatePackage) {

        this.updatePackage = updatePackage;

        Pattern pattern = Pattern.compile("game_(?<in>.*)_(?<out>.*)_hdiff_(?<sign>.*)\\.zip");
        Matcher matcher = pattern.matcher(updatePackage.getName());

        if (!matcher.matches()) {
            throw new IllegalArgumentException("The provided file is not an update package file.");
        }

        this.fromVersion = matcher.group("in");
        this.toVersion   = matcher.group("out");
        this.signature   = matcher.group("sign");
    }

    public static boolean isUpdatePackage(File file) {

        Pattern pattern = Pattern.compile("game_(?<in>.*)_(?<out>.*)_hdiff_(?<sign>.*)\\.zip");
        Matcher matcher = pattern.matcher(file.getName());
        return matcher.matches();
    }

    public File getUpdatePackage() {

        return this.updatePackage;
    }

    public String getFromVersion() {

        return this.fromVersion;
    }

    public String getToVersion() {

        return this.toVersion;
    }

    public String getSignature() {

        return this.signature;
    }
}
