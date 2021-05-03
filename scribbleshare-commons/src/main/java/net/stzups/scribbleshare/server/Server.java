package net.stzups.scribbleshare.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.AttributeKey;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.ScribbleshareConfigKeys;
import net.stzups.scribbleshare.util.LogFactory;

import java.io.File;

public class Server {
    private static final AttributeKey<SslContext> SSL_CONTEXT = AttributeKey.valueOf(Server.class, "SSL_CONTEXT");

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    /**
     * Initializes the server and binds to the specified port
     * @return close future
     */
    public ChannelFuture start(ChannelHandler handler) throws Exception {
        SslContext sslContext;
        if (Scribbleshare.getConfig().getBoolean(ScribbleshareConfigKeys.SSL)) {
            sslContext = SslContextBuilder.forServer(
                            new File(Scribbleshare.getConfig().getString(ScribbleshareConfigKeys.SSL_ROOT_PATH)),
                            new File(Scribbleshare.getConfig().getString(ScribbleshareConfigKeys.SSL_PATH)))
                    .build();
        } else {
            sslContext = null;
        }

        int port = Scribbleshare.getConfig().getInteger(ScribbleshareConfigKeys.PORT);

        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogFactory.getLogger("netty").getName(), LogLevel.DEBUG))
                .childHandler(handler);

        Scribbleshare.getLogger().info("Binding to port " + port);

        ChannelFuture bindFuture = serverBootstrap.bind(port);
        bindFuture.channel().attr(SSL_CONTEXT).set(sslContext);

        ChannelFuture closeFuture = bindFuture.sync().channel().closeFuture();
        //closeFuture.addListener(future -> stop());

        return closeFuture;
    }

    /**
     * Shuts down the server gracefully, then blocks until the server is shut down
     */
    public void stop() throws InterruptedException {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();

        bossGroup.shutdownGracefully().sync();
        workerGroup.shutdownGracefully().sync();
    }

    public static SslContext getSslContext(Channel channel) {
        return channel.attr(SSL_CONTEXT).get();
    }
}