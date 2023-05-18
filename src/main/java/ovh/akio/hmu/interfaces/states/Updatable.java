package ovh.akio.hmu.interfaces.states;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface Updatable {

    /**
     * Retrieve an {@link Optional} {@link File} representing the update package.
     *
     * @return An {@link Optional} {@link File}.
     */
    Optional<File> getUpdatePackageFile();

    /**
     * Extract the update package.
     */
    void extractUpdatePackage() throws IOException;

    /**
     * Retrieve all files from the update package.
     *
     * @return A {@link List} of {@link File}
     */
    List<File> getUpdateFiles();
}
