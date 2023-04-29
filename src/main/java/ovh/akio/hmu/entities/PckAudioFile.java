package ovh.akio.hmu.entities;

import ovh.akio.hmu.Utils;
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

    @Override
    public void onHandled(File output) {

        List<File> unpackedFiles = Utils.getDirectoryContent(output, File::isFile);
        unpackedFiles.stream().map(WemAudioFile::new).forEach(this.outputFiles::add);
    }

    @Override
    public File getSource() {

        return this.source;
    }

    @Override
    public String getName() {

        return this.filename;
    }

    public Stream<WemAudioFile> getOutputFiles() {

        return this.outputFiles.stream();
    }
}
