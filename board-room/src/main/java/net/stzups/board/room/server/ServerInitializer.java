package net.stzups.board.room.server;

import io.netty.channel.Channel;
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
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import io.netty.util.AttributeKey;
import net.stzups.board.BoardConfigKeys;
import net.stzups.board.room.BoardRoomConfigKeys;
import net.stzups.board.room.BoardRoom;
import net.stzups.board.room.server.websocket.WebSocketHandler;
import net.stzups.board.room.server.websocket.protocol.MessageDecoder;
import net.stzups.board.room.server.websocket.protocol.MessageEncoder;
import net.stzups.board.util.LogFactory;

import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Creates pipeline to handle Websocket connections
 * WebSocket connections should be made to the specified WebSocket path
 * Connections not made to the WebSocket path go to ServerHandler
 */
public class ServerInitializer extends ChannelInitializer<SocketChannel> {
    public static final AttributeKey<Logger> LOGGER = AttributeKey.valueOf(ServerInitializer.class, "LOGGER");

    private final GlobalTrafficShapingHandler globalTrafficShapingHandler = new GlobalTrafficShapingHandler(Executors.newSingleThreadScheduledExecutor(), 0, 0, 1000) {
        @Override
        protected void doAccounting(TrafficCounter counter) {
            if (BoardRoom.getConfig().getBoolean(BoardConfigKeys.DEBUG_LOG_TRAFFIC)) System.out.print("\rread " + (double) counter.lastReadThroughput() / 1000 * 8 + "kb/s, write "  + (double) counter.lastWriteThroughput() / 1000 * 8 + "kb/s");
        }
    };

    private Logger logger;
    private SslContext sslContext;
    private final HttpAuthenticator httpAuthenticator = new HttpAuthenticator();
    private final MessageEncoder messageEncoder = new MessageEncoder();
    private final MessageDecoder messageDecoder = new MessageDecoder();

    ServerInitializer(SslContext sslContext) {
        this.sslContext = sslContext;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        logger = LogFactory.getLogger(socketChannel.remoteAddress().toString());
        socketChannel.attr(LOGGER).set(logger);
        logger.info("Initial connection");
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline
                .addLast(new ChannelDuplexHandler() {
                    @Override
                    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) {
                        logger.warning("Uncaught exception");
                        throwable.printStackTrace();
                    }
                })
                .addLast(globalTrafficShapingHandler);
        if (sslContext != null) {
            pipeline.addLast(sslContext.newHandler(socketChannel.alloc()));
        }
        pipeline.addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(65536))
                .addLast(httpAuthenticator)
                .addLast(new WebSocketServerCompressionHandler())
                .addLast(new WebSocketServerProtocolHandler("/", null, true))
                .addLast(messageEncoder)
                .addLast(messageDecoder)
                .addLast(new WebSocketHandler());//todo give this a different executor? https://stackoverflow.com/questions/49133447/how-can-you-safely-perform-blocking-operations-in-a-netty-channel-handler

    }

    public static void setLogger(SocketChannel channel) {
        channel.attr(LOGGER).set(LogFactory.getLogger(channel.remoteAddress().toString()));
    }

    public static Logger getLogger(ChannelHandlerContext ctx) {
        return getLogger(ctx.channel());
    }
    public static Logger getLogger(Channel channel) {
        return channel.attr(LOGGER).get();
    }
}
