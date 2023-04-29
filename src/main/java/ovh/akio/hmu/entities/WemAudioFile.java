package ovh.akio.hmu.entities;

import ovh.akio.hmu.interfaces.AudioFile;

import java.io.File;

public class WemAudioFile implements AudioFile {

    private final File   source;
    private final String filename;
    private       File   output;

    public WemAudioFile(File source) {

        this.source      = source;
        this.filename    = source.getName().substring(0, source.getName().lastIndexOf("."));
    }

    @Override
    public File getSource() {

        return this.source;
    }

    @Override
    public String getName() {

        return this.filename;
    }

    @Override
    public void onHandled(File output) {

        this.output = output;
    }

    public File getOutput() {

        return output;
    }
}
