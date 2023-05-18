package ovh.akio.hmu.exceptions;

import ovh.akio.hmu.interfaces.AudioFile;

public class ExternalProgramException extends RuntimeException {

    private final int       code;
    private final String    output;
    private final String    error;

    /**
     * Constructs a new runtime exception with {@code null} as its detail message.  The cause is not initialized, and
     * may subsequently be initialized by a call to {@link #initCause}.
     */
    public ExternalProgramException(int code, String output, String error) {

        this.code   = code;
        this.output = output;
        this.error  = error;
    }

    public int getCode() {

        return this.code;
    }

    public String getOutput() {

        return this.output;
    }

    public String getError() {

        return this.error;
    }
}
