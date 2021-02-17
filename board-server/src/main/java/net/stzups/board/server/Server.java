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
import net.stzups.board.Board;
import net.stzups.board.LogFactory;

import javax.net.ssl.KeyManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

/**
 * Uses netty to create an HTTP/WebSocket server on the specified port
 */
public class Server {
    private static final int HTTP_PORT = 80;
    private static final int HTTPS_PORT = 443;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    /**
     * Initializes the server and binds to the specified port
     */
    public ChannelFuture start() throws Exception {
        SslContext sslContext;
        int port;

        String keystorePath = Board.getConfig().get("ssl.keystore");
        if (keystorePath != null) {//must not be null
            if (keystorePath.equals("http")) {
                Board.getLogger().warning("Starting server using insecure http:// protocol without SSL");
                sslContext = null;//otherwise sslEngine is null and program continues with unencrypted sockets
                port = HTTP_PORT;
            } else {
                String passphrase = Board.getConfig().get("ssl.passphrase");
                if (passphrase != null) {//can be null if value of keystore is http
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
                        throw new RuntimeException("Exception while getting SSL context", e);
                    }
                } else {
                    throw new RuntimeException("Failed to specify SSL passphrase from --ssl.passphrase flag.");
                }
            }
        } else {
            throw new RuntimeException("Failed to set required flag --ssl.keystore. Perhaps you meant to explicitly disable encrypted sockets over HTTPS using --ssl.keystore http");
        }

        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogFactory.getLogger("netty").getName(), LogLevel.DEBUG))
                .childHandler(new ServerInitializer(sslContext));
        Board.getLogger().info("Binding to port " + port);
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
