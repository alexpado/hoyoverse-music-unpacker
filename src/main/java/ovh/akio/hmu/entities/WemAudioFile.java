package ovh.akio.hmu.entities;

import ovh.akio.hmu.Utils;
import ovh.akio.hmu.enums.AudioState;
import ovh.akio.hmu.interfaces.AudioConverter;
import ovh.akio.hmu.interfaces.AudioFile;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class WemAudioFile implements AudioFile {

    private final File   source;
    private final String filename;
    private       File   output;

    private String     hash;
    private AudioState state;

    public WemAudioFile(File source) {

        this.source   = source;
        this.filename = source.getName().substring(0, source.getName().lastIndexOf("."));
    }

    /**
     * Retrieve which {@link File} should be handled by this {@link AudioFile}.
     *
     * @return The {@link File}.
     */
    @Override
    public File getSource() {

        return this.source;
    }

    /**
     * Retrieve the source filename. It should be the same as {@link #getSource()} witout the file extension.
     *
     * @return The filename.
     */
    @Override
    public String getName() {

        return this.filename;
    }

    /**
     * Called when a conversion from an {@link AudioConverter} was successful.
     *
     * @param output
     *         The {@link File} resulting the conversion. Can be a directory or a file.
     */
    @Override
    public void onHandled(File output) {

        this.output = output;
    }

    /**
     * Retrieve the {@link File} into which the current {@link WemAudioFile} has been extracted.
     *
     * @return A {@link File}, usually a directory.
     */
    public File getOutput() {

        return this.output;
    }

    public void processHash() throws IOException, NoSuchAlgorithmException {

        this.hash = Utils.hashFile(this.source);
    }

    public void setState(AudioState state) {

        this.state = state;
    }

    public String getHash() {

        return this.hash;
    }

    public AudioState getState() {

        return this.state;
    }
}
