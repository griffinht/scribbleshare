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
import net.stzups.scribbleshare.room.ScribbleshareRoomConfigKeys;
import net.stzups.scribbleshare.room.ScribbleshareRoom;
import net.stzups.scribbleshare.util.LogFactory;

import javax.net.ssl.KeyManagerFactory;
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
        SslContext sslContext;
        int port;

        Boolean ssl = ScribbleshareRoom.getConfig().getBoolean(ScribbleshareRoomConfigKeys.SSL);

        if (!ssl) {
            ScribbleshareRoom.getLogger().warning("Starting server using insecure http:// protocol without SSL");
            sslContext = null;//otherwise sslEngine is null and program continues with unencrypted sockets
            port = ScribbleshareRoom.getConfig().getInteger(ScribbleshareRoomConfigKeys.WS_PORT);
        } else {
            String keystorePath = ScribbleshareRoom.getConfig().getString(ScribbleshareRoomConfigKeys.SSL_KEYSTORE_PATH);
            String passphrase = ScribbleshareRoom.getConfig().getString(ScribbleshareRoomConfigKeys.SSL_KEYSTORE_PASSPHRASE);
            try (FileInputStream fileInputStream = new FileInputStream(keystorePath)) {
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                keyStore.load(fileInputStream, passphrase.toCharArray());

                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(keyStore, passphrase.toCharArray());

                //SelfSignedCertificate selfSignedCertificate = new SelfSignedCertificate();
                sslContext = SslContextBuilder.forServer(keyManagerFactory)
                        .sslProvider(SslProvider.JDK)
                        .build();
                port = ScribbleshareRoom.getConfig().getInteger(ScribbleshareRoomConfigKeys.WSS_PORT);
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
        ScribbleshareRoom.getLogger().info("Binding to port " + port);
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
