package ovh.akio.hmu.interfaces;

import java.io.File;

public interface AudioConverter<T extends AudioFile> {

    File handle(T input, File rootPath) throws Exception;

}
