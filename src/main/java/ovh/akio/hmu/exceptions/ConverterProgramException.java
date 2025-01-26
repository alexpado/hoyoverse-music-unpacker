package ovh.akio.hmu.exceptions;

import ovh.akio.hmu.interfaces.AudioFile;

public class ConverterProgramException extends ExternalProgramException {

    private final AudioFile audioFile;

    public ConverterProgramException(int code, String output, String error, AudioFile audioFile) {

        super(code, output, error);
        this.audioFile = audioFile;
    }

    public AudioFile getAudioFile() {

        return this.audioFile;
    }

}
