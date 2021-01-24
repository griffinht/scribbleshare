package net.stzups.board;

import io.netty.channel.ChannelFuture;
import net.stzups.board.config.ConfigProvider;
import net.stzups.board.config.ConfigProviderBuilder;
import net.stzups.board.config.configs.ArgumentConfig;
import net.stzups.board.server.Server;

import java.util.logging.Logger;

public class Board {
    private static Logger logger;
    private static ConfigProvider config;

    public static void main(String[] args) throws Exception {
        logger = LogFactory.getLogger("Board Server");

        logger.info("Starting Board server...");

        long start = System.currentTimeMillis();

        config = new ConfigProviderBuilder()
                .addConfig(new ArgumentConfig(args))
                .build();

        Server server = new Server();
        ChannelFuture channelFuture = server.start();

        logger.info("Started Board server in " + (System.currentTimeMillis() - start) + "ms");

        channelFuture.sync();

        start = System.currentTimeMillis();

        logger.info("Stopping Board Server");

        server.stop();

        logger.info("Stopped Board Server in " + (System.currentTimeMillis() - start) + "ms");
    }

    public static Logger getLogger() {
        return logger;
    }

    public static ConfigProvider getConfig() {
        return config;
    }
}
