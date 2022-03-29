package ovh.akio.genshin.interfaces;

import java.io.File;

public interface AudioFile {

    File getSource();

    String getName();

    void onHandled(File output);

}
