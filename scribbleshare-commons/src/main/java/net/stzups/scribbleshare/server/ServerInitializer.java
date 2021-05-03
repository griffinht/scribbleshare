package net.stzups.scribbleshare.server;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.ScribbleshareConfigKeys;

import java.util.concurrent.Executors;
import java.util.logging.Level;

@ChannelHandler.Sharable
public class ServerInitializer extends ChannelInitializer<SocketChannel> {
    private final GlobalTrafficShapingHandler globalTrafficShapingHandler = new GlobalTrafficShapingHandler(Executors.newSingleThreadScheduledExecutor(), 0, 0, 1000) {
        @Override
        protected void doAccounting(TrafficCounter counter) {
            if (Scribbleshare.getConfig().getBoolean(ScribbleshareConfigKeys.DEBUG_LOG_TRAFFIC)) System.out.print("\rread " + (double) counter.lastReadThroughput() / 1000 * 8 + "kb/s, write "  + (double) counter.lastWriteThroughput() / 1000 * 8 + "kb/s");
        }
    };

    @Override
    protected void initChannel(SocketChannel channel) {
        Scribbleshare.setLogger(channel).info("Connection opened");

        channel.pipeline().addLast(new ChannelDuplexHandler() {
                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
                        Scribbleshare.getLogger(ctx).log(Level.INFO, "Uncaught exception", throwable);
                    }
                })
                .addLast(globalTrafficShapingHandler);

        SslContext sslContext = Server.getSslContext(channel);
        if (sslContext != null) {
            channel.pipeline().addLast(sslContext.newHandler(channel.alloc()));
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        Scribbleshare.getLogger(ctx).info("Connection closed");
    }
}
