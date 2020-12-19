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

public class Server {
    private static final int PORT = 80;

    private ChannelFuture channelFuture;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public void run() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogFactory.getLogger("netty").getName(), LogLevel.INFO))
                .childHandler(new ServerInitializer());
        channelFuture = serverBootstrap.bind(PORT);
        Board.getLogger().info("Listening on port " + PORT);
    }

    public void stop() {
        Board.getLogger().info("Closing HTTP server...");
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        try {
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Board.getLogger().info("Closed HTTP server");
    }
}
