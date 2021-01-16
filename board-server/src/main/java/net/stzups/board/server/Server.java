package net.stzups.board.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import net.stzups.board.Board;
import net.stzups.board.LogFactory;

/**
 * Uses netty to create an HTTP/WebSocket server on the specified port
 */
public class Server {
    private static final int PORT = 80;

    private ChannelFuture channelFuture;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    /**
     * Initializes the server and binds to the specified port
     */
    public void run() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogFactory.getLogger("netty").getName(), LogLevel.DEBUG))
                .childHandler(new ServerInitializer());
        channelFuture = serverBootstrap.bind(PORT);
        Board.getLogger().info("Listening on port " + PORT);
    }

    /**
     * Shuts down the server gracefully, then blocks until the server is shut down
     */
    public void stop() {
        Board.getLogger().info("Closing server...");
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        try {
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Board.getLogger().info("Closed server");
    }
}
