package ovh.akio.hmu;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import ovh.akio.hmu.enums.ListingOption;
import ovh.akio.hmu.interfaces.HoyoverseGame;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class to put stuff that nobody is sure where it should go.
 */
public final class AppUtils {

    /**
     * Maximum length for a {@link ProgressBar} step name. Every step name below this length will be prefixed with the missing
     * amount of character using spaces.
     */
    private static final int STEP_NAME_LENGTH = 14;

    private AppUtils() {}

    /**
     * List the content of the provided directory.
     *
     * @param file
     *         The directory.
     * @param listingOption
     *         The strategy to use when listing content
     *
     * @return A {@link List} of {@link File}.
     */
    public static List<File> getDirectoryContents(File file, ListingOption listingOption) {

        if (!file.isDirectory()) {
            throw new IllegalArgumentException("Please provide a directory");
        }

        File[] files = file.listFiles();
        if (files == null) {
            throw new IllegalStateException("Unable to list files");
        }

        List<File> result = new ArrayList<>();

        switch (listingOption) {
            case RECURSIVE -> {
                for (File content : files) {
                    if (content.isDirectory()) {
                        result.addAll(getDirectoryContents(content, listingOption));
                    } else {
                        result.add(content);
                    }
                }
            }
            case FILES_ONLY -> {
                for (File content : files) {
                    if (content.isFile()) {
                        result.add(content);
                    }
                }
            }
            case NORMAL -> result.addAll(Arrays.asList(files));
        }

        return result;
    }

    /**
     * Walk up the directory structure defined by a list of paths starting from the provided {@link File}. The returned
     * {@link File} will be the directory that was the last entry in the path list provided, and will automatically be created.
     *
     * @param root
     *         The starting point for the directory
     * @param paths
     *         Group of path to follow
     *
     * @return A {@link File} directory, that has been created if it was non-existant.
     */
    public static File walkDirectory(File root, String... paths) {

        File file = root;
        for (String path : paths) {
            file = new File(file, path);
        }
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new IllegalStateException("Unable to create directory " + file.getAbsolutePath());
            }
        }
        return file;
    }

    /**
     * Delete recursively the provided {@link File}. If the provided {@link File} is a regular file, this method will have the
     * same effect as simply calling {@link File#delete()}.
     *
     * @param file
     *         The {@link File} to delete.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteRecursively(File file) {

        if (file.isFile()) {
            file.delete();
            return;
        }

        File[] files = file.listFiles();
        if (files == null) {
            throw new IllegalStateException("Unable to list files");
        }

        for (File subFile : files) {
            if (subFile.isFile()) {
                subFile.delete();
            } else if (subFile.isDirectory()) {
                deleteRecursively(subFile);
                subFile.delete();
            }
        }
    }

    /**
     * Create a {@link ProgressBar}. This method ensure that all {@link ProgressBar} will share a similar design across the
     * application.
     *
     * @param name
     *         The name of the {@link ProgressBar}
     * @param max
     *         The maximum amount of step for the {@link ProgressBar}
     *
     * @return A {@link ProgressBar} instance.
     */
    public static ProgressBar createDefaultProgressBar(CharSequence name, int max) {

        int currentLength = name.length();
        int missing       = STEP_NAME_LENGTH - currentLength;

        if (missing < 0) {
            throw new IllegalArgumentException("Invalid progress bar name: " + name);
        }

        String task = " ".repeat(missing) + name;

        return new ProgressBarBuilder()
                .continuousUpdate()
                .setTaskName(task)
                .setInitialMax(max)
                .setStyle(ProgressBarStyle.ASCII)
                .setUpdateIntervalMillis(100)
                .build();
    }

    /**
     * Easy to access directories.
     */
    public final static class Paths {

        private Paths() {}

        public static Path workspace(HoyoverseGame game) {

            return workspace(game, new File("."));
        }

        public static Path workspace(HoyoverseGame game, File root) {

            return Path.of(root.getAbsolutePath(), "workspace", game.getShortName());
        }

        public static Path unpack(HoyoverseGame game) {

            return Path.of(workspace(game).toString(), "unpacked");
        }

        public static Path extracted(HoyoverseGame game, File root) {

            return Path.of(root.getAbsolutePath(), game.getShortName());
        }

    }

}
