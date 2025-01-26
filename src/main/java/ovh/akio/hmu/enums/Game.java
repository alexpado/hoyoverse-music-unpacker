package ovh.akio.hmu.enums;

import java.io.File;
import java.util.function.Predicate;

public enum Game {

    HSR(file -> file.getName().contains("Minimum") || file.getName().contains("Music")),
    GI(file -> file.getName().contains("Minimum") || file.getName().contains("Music")),
    HI3(file -> file.getName().contains("BGM"));

    private final Predicate<File> defaultFileFilter;

    Game(Predicate<File> defaultFileFilter) {

        this.defaultFileFilter = defaultFileFilter;
    }

    public Predicate<File> getDefaultFileFilter() {

        return this.defaultFileFilter;
    }
}
