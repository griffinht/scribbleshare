package net.stzups.scribbleshare;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import net.stzups.scribbleshare.server.Server;
import net.stzups.scribbleshare.server.ServerInitializer;
import net.stzups.scribbleshare.util.LogFactory;
import net.stzups.scribbleshare.util.config.configs.ArgumentConfig;
import net.stzups.scribbleshare.util.config.configs.EnvironmentVariableConfig;
import net.stzups.scribbleshare.util.config.configs.PropertiesConfig;

import java.util.logging.Logger;

public class Scribbleshare implements AutoCloseable {
    private static final Logger LOGGER = LogFactory.getLogger("scribbleshare");
    public static Logger getLogger() {
        return LOGGER;
    }

    private static final AttributeKey<Logger> LOGGER_KEY = AttributeKey.valueOf(Scribbleshare.class, "LOGGER");
    public static void setLogger(Channel channel) {
        channel.attr(LOGGER_KEY).set(LogFactory.getLogger(channel.remoteAddress().toString()));
    }
    public static Logger getLogger(ChannelHandlerContext ctx) {
        return ctx.channel().attr(LOGGER_KEY).get();
    }



    private final ScribbleshareConfig config;
    private final Server server;

    protected Scribbleshare(String[] args) {
        this(getScribbleshareConfig(args));
    }

    protected Scribbleshare(ScribbleshareConfig config) {
        this.config = config;
        this.server = new Server(config.getPort());
        LogFactory.setLogger(LOGGER, config.getName());
    }

    protected Scribbleshare(ScribbleshareConfig config, Server server) {
        this.config = config;
        this.server = server;
        LogFactory.setLogger(LOGGER, config.getName());
    }

    public ScribbleshareConfig getConfig() {
        return config;
    }

    public ChannelFuture start(ServerInitializer serverInitializer) throws Exception {
        return server.start(serverInitializer);
    }

    @Override
    public void close() throws Exception {
        server.close();
    }

    private static ScribbleshareConfig getScribbleshareConfig(String[] args) {
        ScribbleshareConfigImplementation scribbleshareConfig = new ScribbleshareConfigImplementation();

        scribbleshareConfig
                .addConfigProvider(new ArgumentConfig(args))
                .addConfigProvider(new EnvironmentVariableConfig(scribbleshareConfig.getEnvironmentVariablePrefix()))
                .addConfigProvider(new PropertiesConfig(scribbleshareConfig.getProperties()));

        return scribbleshareConfig;
    }
}
