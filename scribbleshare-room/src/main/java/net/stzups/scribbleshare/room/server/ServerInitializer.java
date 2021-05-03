package net.stzups.scribbleshare.room.server;

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
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.ScribbleshareConfigKeys;
import net.stzups.scribbleshare.room.ScribbleshareRoomConfigKeys;
import net.stzups.scribbleshare.room.server.websocket.ClientMessageHandler;
import net.stzups.scribbleshare.room.server.websocket.protocol.ClientMessageDecoder;
import net.stzups.scribbleshare.room.server.websocket.protocol.ServerMessageEncoder;
import net.stzups.scribbleshare.util.LogFactory;

import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates pipeline to handle Websocket connections
 * WebSocket connections should be made to the specified WebSocket path
 * Connections not made to the WebSocket path go to ServerHandler
 */
public class ServerInitializer extends ChannelInitializer<SocketChannel> {
    private static final AttributeKey<Logger> LOGGER = AttributeKey.valueOf(ServerInitializer.class, "LOGGER");

    private final GlobalTrafficShapingHandler globalTrafficShapingHandler = new GlobalTrafficShapingHandler(Executors.newSingleThreadScheduledExecutor(), 0, 0, 1000) {
        @Override
        protected void doAccounting(TrafficCounter counter) {
            if (Scribbleshare.getConfig().getBoolean(ScribbleshareConfigKeys.DEBUG_LOG_TRAFFIC)) System.out.print("\rread " + (double) counter.lastReadThroughput() / 1000 * 8 + "kb/s, write "  + (double) counter.lastWriteThroughput() / 1000 * 8 + "kb/s");
        }
    };

    private SslContext sslContext;
    private final HttpAuthenticator httpAuthenticator = new HttpAuthenticator();
    private final ServerMessageEncoder serverMessageEncoder = new ServerMessageEncoder();
    private final ClientMessageDecoder clientMessageDecoder = new ClientMessageDecoder();
    private final ClientMessageHandler clientMessageHandler = new ClientMessageHandler();

    ServerInitializer(SslContext sslContext) {
        this.sslContext = sslContext;
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        Logger logger = LogFactory.getLogger(channel.remoteAddress().toString());
        channel.attr(LOGGER).set(logger);

        logger.info("Connection opened");

        ChannelPipeline pipeline = channel.pipeline();
        pipeline
                .addLast(new ChannelDuplexHandler() {
                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
                        getLogger(ctx).log(Level.WARNING, "Uncaught exception", throwable);
                    }
                })
                .addLast(globalTrafficShapingHandler);
        if (sslContext != null) {
            pipeline.addLast(sslContext.newHandler(channel.alloc()));
        }
        pipeline.addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(65536))
                .addLast(httpAuthenticator)
                .addLast(new WebSocketServerCompressionHandler())
                .addLast(new WebSocketServerProtocolHandler(Scribbleshare.getConfig().getString(ScribbleshareRoomConfigKeys.WEBSOCKET_PATH), null, true))
                .addLast(serverMessageEncoder)
                .addLast(clientMessageDecoder)
                .addLast(clientMessageHandler);//todo give this a different executor? https://stackoverflow.com/questions/49133447/how-can-you-safely-perform-blocking-operations-in-a-netty-channel-handler
    }


    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        getLogger(ctx).info("Connection closed");
    }

    public static Logger getLogger(ChannelHandlerContext ctx) {
        return ctx.channel().attr(LOGGER).get();
    }
}
