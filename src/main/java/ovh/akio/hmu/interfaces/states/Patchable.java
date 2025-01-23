package ovh.akio.hmu.interfaces.states;

import ovh.akio.hmu.entities.PckAudioFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Filter;

public interface Patchable extends Updatable {

    /**
     * Patch all files using {@link #getUpdateFiles()}.
     *
     * @param filter The {@link Predicate} to use when filtering files to patch.
     */
    void patch(Predicate<File> filter) throws IOException, InterruptedException;

    /**
     * Retrieve all patched {@link PckAudioFile}.
     *
     * @return A {@link List} of {@link File}.
     */
    List<PckAudioFile> getPatchedFiles();

}
