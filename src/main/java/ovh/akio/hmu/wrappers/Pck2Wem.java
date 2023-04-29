package ovh.akio.hmu.wrappers;

import ovh.akio.hmu.entities.PckAudioFile;
import ovh.akio.hmu.exceptions.WrapperExecutableNotFound;
import ovh.akio.hmu.interfaces.AudioConverter;

import java.io.File;

public class Pck2Wem implements AudioConverter<PckAudioFile> {

    private final File executable;
    private final File metafile;

    public Pck2Wem() {

        this.executable = new File(".", "wrappers\\quickbms\\quickbms.exe");
        this.metafile   = new File(".", "wrappers\\quickbms\\wavescan.bms");

        if (!this.executable.exists()) {
            throw new WrapperExecutableNotFound(this.executable);
        }
    }

    @Override
    public File handle(PckAudioFile input, File outputDirectory) throws Exception {

        File output = new File(outputDirectory, input.getName());
        output.mkdirs();

        String program   = this.executable.getAbsolutePath();
        String waveScan  = this.metafile.getAbsolutePath();
        String inputFile = input.getSource().getAbsolutePath();
        String outputDir = output.getAbsolutePath();

        ProcessBuilder builder = new ProcessBuilder(program, waveScan, inputFile, outputDir);
        Process        process = builder.start();
        process.waitFor();
        if (process.exitValue() > 0) {
            throw new IllegalAccessException("AudioConverter failure: Exit code: " + process.exitValue());
        }

        return output;
    }
}
