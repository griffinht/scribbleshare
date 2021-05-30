package net.stzups.scribbleshare.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Helper class used to format log messages
 */
public class LogFactory {
    private static final DateFormat FORMAT = new SimpleDateFormat("[yyyy-MM-dd] [HH:mm:ss] ");

    /**
     * Creates or finds a Java logger and sets formatting to the following:
     * [yyyy-MM-dd] [hh:mm:ss] [name] [level (if not INFO)] message
     *
     * @param name the name of the logger to create or find
     * @return the created logger
     */
    public static Logger getLogger(String name) {
        Logger logger = Logger.getLogger(name);

        setLogger(logger, name);

        return logger;
    }

    /**
     * Formats an existing logger with a name
     */
    public static void setLogger(Logger logger, String name) {
        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }
        logger.setUseParentHandlers(false);
        Handler handler = new ConsoleHandler();
        logger.addHandler(handler);
        handler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return  FORMAT.format(Date.from(Instant.now()))
                        + ((record.getLevel() == Level.INFO) ? "" : "[" + record.getLevel() + "] ") // include Logger Level if it is not Level.INFO
                        + "[" + name + "] "
                        + record.getMessage()
                        + System.lineSeparator()
                        + ((record.getThrown() == null) ? "" : getStackTrace(record.getThrown()));
            }
        });
    }

    private static String getStackTrace(Throwable throwable) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (PrintStream printStream = new PrintStream(byteArrayOutputStream, true, StandardCharsets.UTF_8)) {
            throwable.printStackTrace(printStream);
        }
        return byteArrayOutputStream.toString(StandardCharsets.UTF_8);
    }
}
