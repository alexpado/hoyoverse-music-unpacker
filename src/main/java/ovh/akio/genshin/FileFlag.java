package ovh.akio.genshin;

import java.io.File;

public enum FileFlag {

    CREATED('C'),
    UPDATED('U'),
    POSSIBLY_DUPLICATED('D'),
    DUPLICATED('-');

    private final char prefix;

    FileFlag(char prefix) {

        this.prefix = prefix;
    }

    public boolean rename(File file) {

        File   parentDirectory = file.getParentFile();
        String newName         = String.format("[%s] %s", this.prefix, file.getName());
        File   newFile         = new File(parentDirectory, newName);

        return file.renameTo(newFile);
    }


}
