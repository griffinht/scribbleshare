package net.stzups.board;

import io.netty.channel.ChannelFuture;
import net.stzups.board.server.Server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

public class Board {
    private static Logger logger;

    public static void main(String[] args) throws Exception {
        logger = LogFactory.getLogger("Board Server");

        Map<String, String> flags = new HashMap<>();
        Iterator<String> iterator = Arrays.asList(args).iterator();
        // format:
        // --flag value
        // --flag "value with space"
        while (iterator.hasNext()) {
            String flag = iterator.next();
            if (flag.startsWith("--") && iterator.hasNext()) {
                String value = iterator.next();
                if (value.startsWith("\"")) {
                    value = value.substring(1);
                    while (iterator.hasNext() && !value.endsWith("\"")) {
                        value += iterator.next();
                    }
                    if (value.endsWith("\"")) {
                        value = value.substring(0, value.length() - 1);
                    }
                }
                flags.put(flag.substring(2), value);
            }
        }
        System.setProperty("DOCUMENT_ROOT_PATH", flags.getOrDefault("document_root_path", "document_root"));
        System.setProperty("DATA_PATH", flags.getOrDefault("data_path", "data"));
        System.setProperty("AUTOSAVE_INTERVAL", flags.getOrDefault("autosave_interval", "-1"));
        logger.info("Starting Board server...");

        long start = System.currentTimeMillis();

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
}
