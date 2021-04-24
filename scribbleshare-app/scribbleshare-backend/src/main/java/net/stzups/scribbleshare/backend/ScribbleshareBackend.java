package net.stzups.scribbleshare.backend;

import io.netty.channel.ChannelFuture;
import net.stzups.scribbleshare.ScribbleshareConfigKeys;
import net.stzups.scribbleshare.backend.server.Server;
import net.stzups.scribbleshare.data.database.ScribbleshareDatabase;
import net.stzups.scribbleshare.data.database.Database;
import net.stzups.scribbleshare.util.LogFactory;
import net.stzups.scribbleshare.util.config.Config;
import net.stzups.scribbleshare.util.config.configs.ArgumentConfig;
import net.stzups.scribbleshare.util.config.configs.EnvironmentVariableConfig;
import net.stzups.scribbleshare.util.config.configs.PropertiesConfig;

import java.util.logging.Logger;

public class ScribbleshareBackend {
    private static final Logger logger = LogFactory.getLogger("scribbleshare-backend");
    private static final Config config = new Config();
    private static Database database;

    public static void main(String[] args) throws Exception {
        logger.info("Starting scribbleshare-backend server...");

        long start = System.currentTimeMillis();

        config.addConfigProvider(new ArgumentConfig(args))
                .addConfigProvider(new EnvironmentVariableConfig("scribbleshare."));
        //this is added last in case the other config strategies have a different value for this
        config.addConfigProvider(new PropertiesConfig(config.getString(ScribbleshareConfigKeys.PROPERTIES)));

        database = new ScribbleshareDatabase(logger, config);

        Server server = new Server();
        ChannelFuture channelFuture = server.start();

        logger.info("Started scribbleshare-backend server in " + (System.currentTimeMillis() - start) + "ms");

        channelFuture.sync();

        start = System.currentTimeMillis();

        logger.info("Stopping scribbleshare-backend server");

        server.stop();

        logger.info("Stopped scribbleshare-backend server in " + (System.currentTimeMillis() - start) + "ms");
    }

    public static Logger getLogger() {
        return logger;
    }

    public static Config getConfig() {
        return config;
    }

    public static Database getDatabase() {
        return database;
    }
}
