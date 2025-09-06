package com.canva.tasktracker.support;

import java.io.IOException;
import java.nio.file.*;
import java.util.logging.*;

public final class LoggerUtil {
    private static Logger LOGGER;

    private LoggerUtil() {}

    public static Logger get() {
        if (LOGGER != null) return LOGGER;

        LOGGER = Logger.getLogger("tasktracker");
        LOGGER.setUseParentHandlers(false); // niente output console

        try {
            Path dir = Path.of(System.getProperty("user.home"), ".dtt");
            if (Files.notExists(dir)) Files.createDirectories(dir);
            Path logFile = dir.resolve("app.log");

            Handler fh = new FileHandler(logFile.toString(), true); // append
            fh.setFormatter(new SimpleFormatter());
            fh.setLevel(Level.INFO);

            LOGGER.addHandler(fh);
            LOGGER.setLevel(Level.INFO);
        } catch (IOException e) {
            // in estremo, se il file log fallisce, abilita console
            ConsoleHandler ch = new ConsoleHandler();
            ch.setLevel(Level.INFO);
            LOGGER.addHandler(ch);
        }

        return LOGGER;
    }
}
