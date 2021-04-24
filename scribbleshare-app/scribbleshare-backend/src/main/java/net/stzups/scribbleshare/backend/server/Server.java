package net.stzups.scribbleshare.backend.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.ScribbleshareConfigKeys;
import net.stzups.scribbleshare.backend.ScribbleshareBackend;
import net.stzups.scribbleshare.backend.ScribbleshareBackendConfigKeys;
import net.stzups.scribbleshare.util.LogFactory;

/**
 * Uses netty to create an HTTP/WebSocket server on the specified port
 */
public class Server {
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    /**
     * Initializes the server and binds to the specified port
     */
    public ChannelFuture start() throws Exception {
        SslContext sslContext;

        int port = Scribbleshare.getConfig().getInteger(ScribbleshareBackendConfigKeys.PORT);

        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogFactory.getLogger("netty").getName(), LogLevel.DEBUG))
                .childHandler(new ServerInitializer(null));
        Scribbleshare.getLogger().info("Binding to port " + port);
        return serverBootstrap.bind(port).sync().channel().closeFuture();
    }

    /**
     * Shuts down the server gracefully, then blocks until the server is shut down
     */
    public void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
