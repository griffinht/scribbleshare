package net.stzups.board.server;

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
import net.stzups.board.BoardConfigKeys;
import net.stzups.board.BoardRoom;
import net.stzups.board.util.LogFactory;

import javax.net.ssl.KeyManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

/**
 * Uses netty to create an HTTP/WebSocket server on the specified port
 */
public class Server {
    private static final int HTTP_PORT = 8080;
    private static final int HTTPS_PORT = 443;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    /**
     * Initializes the server and binds to the specified port
     */
    public ChannelFuture start() throws Exception {
        SslContext sslContext;
        int port;

        Boolean ssl = BoardRoom.getConfig().getBoolean(BoardConfigKeys.SSL);

        if (!ssl) {
            BoardRoom.getLogger().warning("Starting server using insecure http:// protocol without SSL");
            sslContext = null;//otherwise sslEngine is null and program continues with unencrypted sockets
            port = HTTP_PORT;
        } else {
            String keystorePath = BoardRoom.getConfig().getString(BoardConfigKeys.SSL_KEYSTORE_PATH);
            String passphrase = BoardRoom.getConfig().getString(BoardConfigKeys.SSL_KEYSTORE_PASSPHRASE);
            try (FileInputStream fileInputStream = new FileInputStream(keystorePath)) {
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                keyStore.load(fileInputStream, passphrase.toCharArray());

                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(keyStore, passphrase.toCharArray());

                //SelfSignedCertificate selfSignedCertificate = new SelfSignedCertificate();
                sslContext = SslContextBuilder.forServer(keyManagerFactory)
                        .sslProvider(SslProvider.JDK)
                        .build();
                port = HTTPS_PORT;
            } catch (IOException | GeneralSecurityException e) {
                throw new Exception("Exception while getting SSL context", e);
            }
        }

        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogFactory.getLogger("netty").getName(), LogLevel.DEBUG))
                .childHandler(new ServerInitializer(sslContext));
        BoardRoom.getLogger().info("Binding to port " + port);
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
