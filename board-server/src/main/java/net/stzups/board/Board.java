package net.stzups.board;

import net.stzups.board.server.Server;

import java.util.logging.Logger;

public class Board {
    private static Logger logger;

    public static void main(String[] args) {
        logger = LogFactory.getLogger("Board Server");

        logger.info("Starting Board server...");

        long start = System.currentTimeMillis();

        Server server = new Server();
        ChannelFuture channelFuture = server.start();

        logger.info("Started Board server in " + (System.currentTimeMillis() - start) + "ms");
    }

    static void stop() {
        logger.info("Stopping Board server...");

        long start = System.currentTimeMillis();

        server.stop();

        logger.info("Stopped Board server in " + (System.currentTimeMillis() - start) + "ms");
    }

    public static Logger getLogger() {
        return logger;
    }
}
