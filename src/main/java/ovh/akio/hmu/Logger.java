package ovh.akio.hmu;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class Logger {

    private Logger() {}

    private static String now() {

        LocalDateTime     now       = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return now.format(formatter);
    }

    public static void info(String message, Object... args) {

        System.out.printf("[%s] [?] %s%n", now(), String.format(message, args));
    }

    public static void warn(String message, Object... args) {

        System.out.printf("[%s] [!] %s%n", now(), String.format(message, args));
    }

    public static void error(String message, Object... args) {

        System.err.printf("[%s] [!] %s%n", now(), String.format(message, args));
    }

}
