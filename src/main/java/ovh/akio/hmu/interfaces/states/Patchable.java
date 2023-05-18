package ovh.akio.hmu.interfaces.states;

import ovh.akio.hmu.entities.PckAudioFile;

import java.io.File;
import java.util.List;

public interface Patchable extends Updatable {

    /**
     * Patch all files using {@link #getUpdateFiles()}.
     */
    void patch();

    /**
     * Retrieve all patched {@link PckAudioFile}.
     *
     * @return A {@link List} of {@link File}.
     */
    List<PckAudioFile> getPatchedFiles();

}
