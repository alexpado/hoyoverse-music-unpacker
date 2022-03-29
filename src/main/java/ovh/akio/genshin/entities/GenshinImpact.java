package ovh.akio.genshin.entities;

import ovh.akio.genshin.Utils;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GenshinImpact {

    private final File file;
    private final File updatePackage;

    public GenshinImpact(File file) {

        if (!Utils.isGenshinDirectory(file)) {
            throw new IllegalArgumentException("Invalid genshin folder.");
        }

        this.file = file;
        File       gameDir   = new File(this.file, "Genshin Impact Game");
        List<File> gameFiles = Utils.getDirectoryContent(gameDir);
        this.updatePackage = gameFiles.stream().filter(UpdatePackage::isUpdatePackage).findFirst().orElse(null);
    }

    public Optional<UpdatePackage> getUpdatePackage() {

        return Optional.ofNullable(this.updatePackage).map(UpdatePackage::new);
    }

    public List<PckAudioFile> getAudioFiles() {

        File soundFolder = new File(this.file, "Genshin Impact Game\\GenshinImpact_Data\\StreamingAssets\\Audio\\GeneratedSoundBanks\\Windows");

        if (!soundFolder.exists()) {
            throw new IllegalStateException("Unable to find sound folder... Is the game really installed ?");
        }

        List<File> audioFiles = Utils.getDirectoryContent(soundFolder, file -> {
            return file.isFile() && (file.getName().startsWith("Minimum") || file.getName().startsWith("Music"));
        });
        return audioFiles.stream().map(PckAudioFile::new).collect(Collectors.toList());
    }
}
