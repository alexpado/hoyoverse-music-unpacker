package ovh.akio.hmu.exceptions;

import java.io.File;

public class WrapperExecutableNotFound extends RuntimeException {

    private final File file;

    /**
     * Constructs a new runtime exception with {@code null} as its detail message.  The cause is not initialized, and
     * may subsequently be initialized by a call to {@link #initCause}.
     */
    public WrapperExecutableNotFound(File file) {

        super("Unable to access file " + file.getPath());
        this.file = file;
    }

    public File getFile() {

        return this.file;
    }
}
