package net.stzups.scribbleshare;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import net.stzups.scribbleshare.util.LogFactory;
import net.stzups.scribbleshare.util.config.Config;

import java.util.logging.Logger;

public class Scribbleshare {
    private static final AttributeKey<Logger> LOGGER = AttributeKey.valueOf(Scribbleshare.class, "LOGGER");

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

    public static Logger setLogger(Channel channel) {
        Logger logger = LogFactory.getLogger(channel.remoteAddress().toString());
        channel.attr(LOGGER).set(logger);
        return logger;
    }

    public static Logger getLogger(ChannelHandlerContext ctx) {
        return ctx.channel().attr(LOGGER).get();
    }
}
