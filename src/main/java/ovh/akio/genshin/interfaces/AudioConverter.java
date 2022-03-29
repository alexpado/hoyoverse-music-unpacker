package ovh.akio.genshin.interfaces;

import java.io.File;

public interface AudioConverter<T extends AudioFile> {

    File handle(T input, File rootPath) throws Exception;

}
