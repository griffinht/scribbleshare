package net.stzups.scribbleshare.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import io.netty.util.AttributeKey;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.ScribbleshareConfigKeys;
import net.stzups.scribbleshare.util.LogFactory;

import java.io.File;

public class Server implements AutoCloseable {
    private static final AttributeKey<SslContext> SSL_CONTEXT = AttributeKey.valueOf(Server.class, "SSL_CONTEXT");

    private final SslContext sslContext;
    private final int port;

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    public Server() throws Exception {
        if (Scribbleshare.getConfig().getBoolean(ScribbleshareConfigKeys.SSL)) {
            sslContext = SslContextBuilder.forServer(
                    new File(Scribbleshare.getConfig().getString(ScribbleshareConfigKeys.SSL_ROOT_PATH)),
                    new File(Scribbleshare.getConfig().getString(ScribbleshareConfigKeys.SSL_PATH)))
                    .build();
        } else {
            sslContext = null;
        }

        port = Scribbleshare.getConfig().getInteger(ScribbleshareConfigKeys.PORT);

        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
    }

    /**
     * Initializes the server and binds to the specified port
     * @return close future
     */
    public ChannelFuture start(ChannelHandler handler) throws Exception {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogFactory.getLogger("netty").getName(), LogLevel.DEBUG))
                .childHandler(handler);

        ChannelFuture bindFuture = serverBootstrap.bind(port).await();
        if (!bindFuture.isSuccess()) {
            throw new Exception("Failed to bind to port " + port, bindFuture.cause());
        }

        bindFuture.channel().pipeline().addLast(new GlobalTrafficShapingHandler(bindFuture.channel().eventLoop(), 0, 0, 1000) {
            @Override
            protected void doAccounting(TrafficCounter counter) {
                if (Scribbleshare.getConfig().getBoolean(ScribbleshareConfigKeys.DEBUG_LOG_TRAFFIC)) System.out.print("\rread " + (double) counter.lastReadThroughput() / 1000 * 8 + "kb/s, write "  + (double) counter.lastWriteThroughput() / 1000 * 8 + "kb/s");
            }
        });

        Scribbleshare.getLogger().info("Bound to port " + port);

        bindFuture.channel().attr(SSL_CONTEXT).set(sslContext);

        return bindFuture.channel().closeFuture();
    }

    @Override
    public void close() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }


    public static SslContext getSslContext(Channel channel) {
        return channel.attr(SSL_CONTEXT).get();
    }
}