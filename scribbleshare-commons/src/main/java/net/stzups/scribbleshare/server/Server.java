package net.stzups.scribbleshare.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.util.LogFactory;

public class Server implements AutoCloseable {
    private final int port;

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    public Server() {
        port = Scribbleshare.getConfig().getPort();

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

        Scribbleshare.getLogger().info("Bound to port " + port);

        return bindFuture.channel().closeFuture();
    }

    @Override
    public void close() {
        workerGroup.shutdownGracefully().syncUninterruptibly();
        bossGroup.shutdownGracefully().syncUninterruptibly();
    }
}