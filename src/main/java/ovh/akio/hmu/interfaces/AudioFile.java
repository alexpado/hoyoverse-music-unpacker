package ovh.akio.hmu.interfaces;

import java.io.File;

/**
 * Interface representing an Audio File that can be converted into another format by an {@link AudioConverter}.
 */
public interface AudioFile {

    /**
     * Retrieve which {@link File} should be handled by this {@link AudioFile}.
     *
     * @return The {@link File}.
     */
    File getSource();

    /**
     * Retrieve the source filename. It should be the same as {@link #getSource()} witout the file extension.
     *
     * @return The filename.
     */
    String getName();

    /**
     * Called when a conversion from an {@link AudioConverter} was successful.
     *
     * @param output
     *         The {@link File} resulting the conversion. Can be a directory or a file.
     */
    void onHandled(File output);

}
