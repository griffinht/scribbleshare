package net.stzups.board.backend;

import io.netty.channel.ChannelFuture;
import net.stzups.board.backend.server.Server;
import net.stzups.board.data.database.BoardDatabase;
import net.stzups.board.data.database.Database;
import net.stzups.board.util.LogFactory;
import net.stzups.board.util.config.Config;
import net.stzups.board.util.config.configs.ArgumentConfig;
import net.stzups.board.util.config.configs.EnvironmentVariableConfig;

import java.util.logging.Logger;

public class BoardBackend {
    private static final Logger logger = LogFactory.getLogger("BoardBackend");
    private static final Config config = new Config();
    private static Database database;

    public static void main(String[] args) throws Exception {
        logger.info("Starting Board Backend server...");

        long start = System.currentTimeMillis();

        config.addConfigProvider(new EnvironmentVariableConfig("board"))
                .addConfigProvider(new ArgumentConfig(args));

        database = new BoardDatabase(logger, config);

        Server server = new Server();
        ChannelFuture channelFuture = server.start();

        logger.info("Started Board Room server in " + (System.currentTimeMillis() - start) + "ms");

        channelFuture.sync();

        start = System.currentTimeMillis();

        logger.info("Stopping Board Room server");

        server.stop();

        logger.info("Stopped Board Room server in " + (System.currentTimeMillis() - start) + "ms");
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
