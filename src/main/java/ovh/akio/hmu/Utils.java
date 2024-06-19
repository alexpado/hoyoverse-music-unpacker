package ovh.akio.hmu;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import ovh.akio.hmu.entities.PckAudioFile;

import java.io.*;
import java.math.BigInteger;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Utils {

    public static File asLocalDirectory(File root, String... path) {
        File file = root;

        for (String s : path) {
            file = new File(file, s);
        }

        if (!file.exists()) {
            file.mkdirs();
        }

        return file;
    }

    public static File asLocalDirectory(String... path) {
        return asLocalDirectory( new File("."), path);
    }

    public static File asLocalFile(File root, String... path) {

        File file = root;
        for (String s : path) {
            file = new File(file, s);
        }

        File parentDir = file.getParentFile();

        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        return file;
    }

    public static File asLocalFile(String... path) {

        return asLocalFile(new File("."), path);
    }

    public static void delete(File file) {

        if (file.isFile()) {
            file.delete();
        } else if (file.isDirectory()) {
            List<File> content = Utils.getDirectoryContent(file);
            content.forEach(Utils::delete);
            file.delete();
        }
    }

    public static ProgressBar defaultProgressBar(String name, int max) {

        return new ProgressBarBuilder()
                .continuousUpdate()
                .setTaskName(name)
                .setInitialMax(max)
                .setStyle(ProgressBarStyle.ASCII)
                .setUpdateIntervalMillis(100)
                .build();
    }

    public static String hashFile(File f) throws IOException, NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-512");

        try (BufferedInputStream in = new BufferedInputStream((new FileInputStream(f))) ;
             DigestOutputStream out = new DigestOutputStream(OutputStream.nullOutputStream(), md)) {
            in.transferTo(out);
        }

        String fx = "%0" + (md.getDigestLength() * 2) + "x";
        return String.format(fx, new BigInteger(1, md.digest()));
    }

    public static List<File> getDirectoryContent(File directory) {

        return getDirectoryContent(directory, f -> true);
    }

    public static List<File> getDirectoryContent(File directory, Predicate<File> predicate) {

        File[] files = directory.listFiles();
        if (files != null && files.length > 0) {
            return Arrays.stream(files).filter(predicate).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static List<PckAudioFile> scanPck(File path) {

        if (!path.exists()) {
            System.err.println("Not an audio output directory: " + path.getAbsolutePath());
            return Collections.emptyList();
        }

        if (!Application.MAIN.AllowAnyAudioFiles) {
            return Utils.getDirectoryContent(path)
                    .stream()
                    .filter(File::isFile)
                    .filter(file -> file.getName().endsWith(".pck"))
                    .map(PckAudioFile::new)
                    .toList();
        } else {
            return Utils.getDirectoryContent(path)
                    .stream()
                    .filter(File::isFile)
                    .filter(file -> file.getName().endsWith(".pck"))
                    .filter(file -> file.getName().startsWith("Minimum") || file.getName().startsWith("Music"))
                    .map(PckAudioFile::new)
                    .toList();
        }
    }
}
