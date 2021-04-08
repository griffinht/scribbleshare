package net.stzups.board.backend;

import io.netty.channel.ChannelFuture;
import net.stzups.board.backend.server.Server;
import net.stzups.board.util.LogFactory;

import java.util.logging.Logger;

public class BoardBackend {
    private static final Logger logger = LogFactory.getLogger("BoardBackend");

    public static void main(String[] args) throws Exception {
        logger.info("Starting Board Backend server...");

        long start = System.currentTimeMillis();

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
}
