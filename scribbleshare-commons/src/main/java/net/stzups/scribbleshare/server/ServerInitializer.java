package net.stzups.scribbleshare.server;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.ScribbleshareConfigImplementation;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.logging.Level;

@ChannelHandler.Sharable
public class ServerInitializer extends ChannelInitializer<SocketChannel> {
    public interface Config {
        boolean getSSL();
        String getSSLRootPath();
        String getSSLPath();
    }
    private final SslContext sslContext;

    protected ServerInitializer(Config config) throws SSLException {
        if (config.getSSL()) {
            sslContext = SslContextBuilder.forServer(
                    new File(config.getSSLRootPath()),
                    new File(config.getSSLPath()))
                    .build();
        } else {
            sslContext = null;
        }
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        Scribbleshare.setLogger(channel);

        channel.pipeline()
                .addLast(new ChannelDuplexHandler() {
                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
                        Scribbleshare.getLogger(ctx).log(Level.WARNING, "Uncaught exception", throwable);
                    }

                    @Override
                    public void channelActive(ChannelHandlerContext ctx) {
                        Scribbleshare.getLogger(ctx).info("Connection opened");
                        ctx.fireChannelActive();
                    }

                    @Override
                    public void channelInactive(ChannelHandlerContext ctx) {
                        Scribbleshare.getLogger(ctx).info("Connection closed");
                        ctx.fireChannelInactive();
                    }
                }).addLast(new GlobalTrafficShapingHandler(channel.eventLoop(), 0, 0, 1000) {
                    @Override
                    protected void doAccounting(TrafficCounter counter) {
                        if (Scribbleshare.getConfig().getBoolean(ScribbleshareConfigImplementation.DEBUG_LOG_TRAFFIC)) System.out.print("\rread " + (double) counter.lastReadThroughput() / 1000 * 8 + "kb/s, write "  + (double) counter.lastWriteThroughput() / 1000 * 8 + "kb/s");
                    }
                });
        if (sslContext != null) {
            channel.pipeline().addLast(sslContext.newHandler(channel.alloc()));
        }
    }
}
