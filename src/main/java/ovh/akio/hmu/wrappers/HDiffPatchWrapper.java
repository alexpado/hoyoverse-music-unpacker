package ovh.akio.hmu.wrappers;

import ovh.akio.hmu.entities.PckAudioFile;
import ovh.akio.hmu.exceptions.WrapperExecutableNotFound;

import java.io.File;

public class HDiffPatchWrapper {

    private final File executable;

    public HDiffPatchWrapper() {

        this.executable = new File(".", "wrappers\\hdiffpatch\\hpatchz.exe");

        if (!this.executable.exists()) {
            throw new WrapperExecutableNotFound(this.executable);
        }
    }

    public PckAudioFile patch(File sourceFile, File patchFile, File outputFile) throws Exception {

        String program    = this.executable.getAbsolutePath();
        String oldPath    = sourceFile.getAbsolutePath();
        String diffPath   = patchFile.getAbsolutePath();
        String outNewPath = outputFile.getAbsolutePath();

        ProcessBuilder builder = new ProcessBuilder(program, oldPath, diffPath, outNewPath);
        Process        process = builder.start();
        process.waitFor();
        if (process.exitValue() > 0) {
            throw new IllegalAccessException("AudioConverter failure: Exit code: " + process.exitValue());
        }

        return new PckAudioFile(outputFile);
    }

}
