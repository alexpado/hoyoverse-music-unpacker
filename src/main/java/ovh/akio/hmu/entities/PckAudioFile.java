package ovh.akio.hmu.entities;

import ovh.akio.hmu.Utils;
import ovh.akio.hmu.interfaces.AudioConverter;
import ovh.akio.hmu.interfaces.AudioFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class PckAudioFile implements AudioFile {

    private final File               source;
    private final String             filename;
    private final List<WemAudioFile> outputFiles;

    public PckAudioFile(File source) {

        this.source      = source;
        this.filename    = source.getName().substring(0, source.getName().lastIndexOf("."));
        this.outputFiles = new ArrayList<>();
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

        List<File> unpackedFiles = Utils.getDirectoryContent(output, File::isFile);
        unpackedFiles.stream().map(WemAudioFile::new).forEach(this.outputFiles::add);
    }

    /**
     * Retrieve the {@link Stream} containing every {@link WemAudioFile} extracted from the current {@link PckAudioFile}
     * instance.
     * <p>
     * The stream will contain items only, and only if {@link #onHandled(File)} has been called at least once.
     *
     * @return A {@link Stream} of {@link WemAudioFile}.
     */
    public Stream<WemAudioFile> getOutputFiles() {

        return this.outputFiles.stream();
    }
}
