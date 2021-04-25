package net.stzups.scribbleshare;

import net.stzups.scribbleshare.util.LogFactory;
import net.stzups.scribbleshare.util.config.Config;

import java.util.logging.Logger;

public class Scribbleshare {
    private static final Logger logger = LogFactory.getLogger("Scribbleshare");
    private static final Config config = new Config();

    public static void setLogger(String name) {
        LogFactory.setLogger(logger, name);
    }

    public static Logger getLogger() {
        return logger;
    }

    public static Config getConfig() {
        return config;
    }
}
