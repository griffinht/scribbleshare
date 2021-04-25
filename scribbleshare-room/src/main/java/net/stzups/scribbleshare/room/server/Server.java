package net.stzups.scribbleshare.room.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.ScribbleshareConfigKeys;
import net.stzups.scribbleshare.room.ScribbleshareRoomConfigKeys;
import net.stzups.scribbleshare.room.ScribbleshareRoom;
import net.stzups.scribbleshare.util.LogFactory;

import javax.net.ssl.KeyManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

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
        int port = Scribbleshare.getConfig().getInteger(ScribbleshareConfigKeys.PORT);

        SslContext sslContext;

        if (Scribbleshare.getConfig().getBoolean(ScribbleshareConfigKeys.SSL)) {
            sslContext = SslContextBuilder.forServer(new File(Scribbleshare.getConfig().getString(ScribbleshareConfigKeys.SSL_ROOT_PATH)),
                    new File(Scribbleshare.getConfig().getString(ScribbleshareConfigKeys.SSL_PATH))).build();
        } else {
            sslContext = null;
        }

        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogFactory.getLogger("netty").getName(), LogLevel.DEBUG))
                .childHandler(new ServerInitializer(sslContext));
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
