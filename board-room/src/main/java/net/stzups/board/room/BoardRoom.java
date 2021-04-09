package net.stzups.board.room;

import io.netty.channel.ChannelFuture;
import net.stzups.board.data.database.BoardDatabase;
import net.stzups.board.data.database.Database;
import net.stzups.board.room.server.Server;
import net.stzups.board.util.LogFactory;
import net.stzups.board.util.config.Config;
import net.stzups.board.util.config.configs.ArgumentConfig;
import net.stzups.board.util.config.configs.EnvironmentVariableConfig;
import net.stzups.board.util.config.configs.PropertiesConfig;

import java.util.logging.Logger;

public class BoardRoom {
    private static Logger logger;
    private static Config config;

    private static Database database;

    public static void main(String[] args) throws Exception {
        logger = LogFactory.getLogger("Board Room");

        logger.info("Starting Board Room server...");

        long start = System.currentTimeMillis();

        config = new Config()
                .addConfigProvider(new ArgumentConfig(args))
                .addConfigProvider(new EnvironmentVariableConfig("board."));
        //this is added last in case the other config strategies have a different value for this
        config.addConfigProvider(new PropertiesConfig(config.getString(BoardRoomConfigKeys.BOARD_PROPERTIES)));

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
