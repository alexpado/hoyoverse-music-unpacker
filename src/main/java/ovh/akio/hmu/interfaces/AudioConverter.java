package ovh.akio.hmu.interfaces;

import java.io.File;
import java.io.IOException;

public interface AudioConverter<T extends AudioFile> {

    File handle(T input, File rootPath) throws IOException, InterruptedException;

}
