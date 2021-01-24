package net.stzups.board.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import net.stzups.board.Board;
import net.stzups.board.protocol.PacketEncoder;
import net.stzups.board.protocol.PacketDecoder;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.concurrent.Executors;

/**
 * Creates pipeline to handle HTTP requests and WebSocket connections on the same port
 * WebSocket connections should be made to the specified WebSocket path
 * Connections not made to the WebSocket path go to ServerHandler
 */
public class ServerInitializer extends ChannelInitializer<SocketChannel> {
    private static final String WEB_SOCKET_PATH = "/websocket";
    private static final boolean DEBUG_LOG_TRAFFIC = Boolean.parseBoolean(Board.getConfig().get("debug.log.traffic", "false"));

    private GlobalTrafficShapingHandler globalTrafficShapingHandler = new GlobalTrafficShapingHandler(Executors.newSingleThreadScheduledExecutor(), 0, 0, 1000) {
        @Override
        protected void doAccounting(TrafficCounter counter) {
            if (DEBUG_LOG_TRAFFIC) System.out.print("\rread " + (double) counter.lastReadThroughput() / 1000 * 8 + "kb/s, write "  + (double) counter.lastWriteThroughput() / 1000 * 8 + "kb/s");
        }
    };

    private PacketEncoder packetEncoder = new PacketEncoder();
    private PacketDecoder packetDecoder = new PacketDecoder();
    private WebSocketInitializer webSocketInitializer = new WebSocketInitializer();
    private static SSLEngine sslEngine;

    static {
        char[] passphrase = Board.getConfig().getCharArray("ssl.passphrase");
        String keystorePath = Board.getConfig().get("ssl.keystore");

        if (passphrase != null && keystorePath != null) {
            try (FileInputStream fileInputStream = new FileInputStream(keystorePath)) {
                sslEngine = sslContextFactory(passphrase, fileInputStream).createSSLEngine();
            } catch (IOException | GeneralSecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private static SSLContext sslContextFactory(char[] passphrase, FileInputStream keystore) throws IOException, GeneralSecurityException {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(keystore, passphrase);

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, passphrase);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
        return sslContext;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        Board.getLogger().info("New connection from " + socketChannel.remoteAddress());
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(globalTrafficShapingHandler);
        if (sslEngine != null) pipeline.addLast(new SslHandler(sslEngine));
        pipeline.addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(65536))
                .addLast(new ChunkedWriteHandler())
                .addLast(new WebSocketServerCompressionHandler())
                .addLast(new WebSocketServerProtocolHandler(WEB_SOCKET_PATH, null, true))
                .addLast(new HttpServerHandler())
                .addLast(packetEncoder)
                .addLast(packetDecoder)
                .addLast(webSocketInitializer);
    }
}
