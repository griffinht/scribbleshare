package net.stzups.scribbleshare.backend.server;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.ScribbleshareConfigKeys;
import net.stzups.scribbleshare.backend.server.http.HttpServerHandler;

import java.util.concurrent.Executors;

/**
 * Creates pipeline to handle Websocket connections
 * WebSocket connections should be made to the specified WebSocket path
 * Connections not made to the WebSocket path go to ServerHandler
 */
public class ServerInitializer extends ChannelInitializer<SocketChannel> {
    private final GlobalTrafficShapingHandler globalTrafficShapingHandler = new GlobalTrafficShapingHandler(Executors.newSingleThreadScheduledExecutor(), 0, 0, 1000) {
        @Override
        protected void doAccounting(TrafficCounter counter) {
            if (Scribbleshare.getConfig().getBoolean(ScribbleshareConfigKeys.DEBUG_LOG_TRAFFIC)) System.out.print("\rread " + (double) counter.lastReadThroughput() / 1000 * 8 + "kb/s, write "  + (double) counter.lastWriteThroughput() / 1000 * 8 + "kb/s");
        }
    };

    private final SslContext sslContext;
    private final HttpServerHandler httpServerHandler = new HttpServerHandler();

    ServerInitializer(SslContext sslContext) {
        this.sslContext = sslContext;
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        Scribbleshare.setLogger(channel).info("Connection");

        ChannelPipeline pipeline = channel.pipeline();
        pipeline
                .addLast(new ChannelDuplexHandler() {
                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
                        Scribbleshare.getLogger(ctx).warning("Uncaught exception");
                        throwable.printStackTrace();
                    }
                })
                .addLast(globalTrafficShapingHandler);
        if (sslContext != null) {
            pipeline.addLast(sslContext.newHandler(channel.alloc()));
        }
        pipeline
                .addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(Integer.MAX_VALUE)) //2gb todo decrease
                .addLast(new HttpContentCompressor())
                .addLast(new ChunkedWriteHandler())
                .addLast(httpServerHandler);
    }
}
