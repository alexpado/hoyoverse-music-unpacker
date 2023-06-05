package ovh.akio.hmu.wrappers;

import ovh.akio.hmu.entities.PckAudioFile;
import ovh.akio.hmu.exceptions.ConverterProgramException;
import ovh.akio.hmu.exceptions.WrapperExecutableNotFound;

import java.io.File;
import java.io.IOException;

public class HDiffPatchWrapper {

    private final File executable;

    public HDiffPatchWrapper() {

        this.executable = new File(".", "wrappers\\hdiffpatch\\hpatchz.exe");

        if (!this.executable.exists()) {
            throw new WrapperExecutableNotFound(this.executable);
        }
    }

    public PckAudioFile patch(PckAudioFile sourceFile, File patchFile, File outputFile) throws IOException, InterruptedException {

        String program    = this.executable.getAbsolutePath();
        String oldPath    = sourceFile.getSource().getAbsolutePath();
        String diffPath   = patchFile.getAbsolutePath();
        String outNewPath = outputFile.getAbsolutePath();

        ProcessBuilder builder = new ProcessBuilder(program, oldPath, diffPath, outNewPath);
        Process        process = builder.start();
        process.waitFor();
        if (process.exitValue() > 0) {
            String processOutput = new String(process.getInputStream().readAllBytes());
            String processError = new String(process.getErrorStream().readAllBytes());

            throw new ConverterProgramException(process.exitValue(), processOutput, processError, sourceFile);
        }

        return new PckAudioFile(outputFile);
    }

}
