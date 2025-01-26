package ovh.akio.hmu.wrappers;

import ovh.akio.hmu.AppUtils;
import ovh.akio.hmu.entities.WemAudioFile;
import ovh.akio.hmu.exceptions.ConverterProgramException;
import ovh.akio.hmu.exceptions.WrapperExecutableNotFound;
import ovh.akio.hmu.interfaces.AudioConverter;

import java.io.File;
import java.io.IOException;

public class Wem2Wav implements AudioConverter<WemAudioFile> {

    private final File executable;

    public Wem2Wav() {

        this.executable = new File(".", "wrappers\\vgmstream\\vgmstream.exe");

        if (!this.executable.exists()) {
            throw new WrapperExecutableNotFound(this.executable);
        }
    }

    @Override
    public File handle(WemAudioFile input, File outputDirectory) throws IOException, InterruptedException {

        File   category   = input.getSource().getParentFile();
        String outputName = String.format("%s.wav", input.getName());

        File categoryOutput = AppUtils.walkDirectory(outputDirectory, category.getName());
        File output         = new File(categoryOutput, outputName);

        String program    = this.executable.getAbsolutePath();
        String inputFile  = input.getSource().getAbsolutePath();
        String outputFile = output.getAbsolutePath();

        ProcessBuilder builder = new ProcessBuilder(program, "-o", outputFile, inputFile);
        Process        process = builder.start();
        process.waitFor();

        if (process.exitValue() > 0) {
            String processOutput = new String(process.getInputStream().readAllBytes());
            String processError  = new String(process.getErrorStream().readAllBytes());

            throw new ConverterProgramException(process.exitValue(), processOutput, processError, input);
        }

        return output;
    }

}
