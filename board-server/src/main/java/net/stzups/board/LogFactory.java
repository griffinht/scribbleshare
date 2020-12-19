package net.stzups.board;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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
    /**
     * Creates or finds a Java logger and sets formatting to the following:
     * [hh:mm:ss] [name] [level (if not INFO)] message
     *
     * @param name the name of the logger to create or find
     * @return the created logger
     */
    public static Logger getLogger(String name) {
        Logger logger = Logger.getLogger(name);
        logger.setUseParentHandlers(false);
        Handler handler = new ConsoleHandler();
        logger.addHandler(handler);

        handler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                String level = (record.getLevel() == Level.INFO) ? "" : "[" + record.getLevel() + "] ";
                return "["
                        + new SimpleDateFormat("HH:mm:ss").format(new Timestamp(System.currentTimeMillis()))
                        + "] ["
                        + name
                        + "] "
                        + level
                        + record.getMessage()
                        + System.lineSeparator();
            }
        });

        return logger;
    }
}
