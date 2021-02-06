package net.stzups.board.server;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import net.stzups.board.Board;
import net.stzups.board.server.websocket.protocol.PacketEncoder;
import net.stzups.board.server.websocket.protocol.PacketDecoder;
import net.stzups.board.server.http.HttpServerHandler;

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
    private SslContext sslContext;

    ServerInitializer(SslContext sslContext) {
        this.sslContext = sslContext;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        Board.getLogger().info("New connection from " + socketChannel.remoteAddress());
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline
                .addLast(new ChannelDuplexHandler() {
                    @Override
                    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) {
                        Board.getLogger().warning("Uncaught exception");
                        throwable.printStackTrace();
                    }
                })
                .addLast(globalTrafficShapingHandler);
        if (sslContext != null) {
            pipeline.addLast(sslContext.newHandler(socketChannel.alloc()));
        }
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
