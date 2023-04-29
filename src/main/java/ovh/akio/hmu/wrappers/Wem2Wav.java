package ovh.akio.hmu.wrappers;

import ovh.akio.hmu.entities.WemAudioFile;
import ovh.akio.hmu.exceptions.WrapperExecutableNotFound;
import ovh.akio.hmu.interfaces.AudioConverter;

import java.io.File;

public class Wem2Wav implements AudioConverter<WemAudioFile> {

    private final File executable;

    public Wem2Wav() {

        this.executable = new File(".", "wrappers\\vgmstream\\vgmstream.exe");

        if (!this.executable.exists()) {
            throw new WrapperExecutableNotFound(this.executable);
        }
    }

    @Override
    public File handle(WemAudioFile input, File outputDirectory) throws Exception {

        File   output     = new File(outputDirectory, input.getName() + ".wav");
        String program    = this.executable.getAbsolutePath();
        String inputFile  = input.getSource().getAbsolutePath();
        String outputFile = output.getAbsolutePath();

        ProcessBuilder builder = new ProcessBuilder(program, "-o", outputFile, inputFile);
        Process        process = builder.start();
        process.waitFor();
        if (process.exitValue() > 0) {
            throw new IllegalAccessException("AudioConverter failure: Exit code: " + process.exitValue());
        }

        return output;
    }
}
