package net.stzups.scribbleshare.server;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import net.stzups.scribbleshare.Scribbleshare;

import java.util.logging.Level;

@ChannelHandler.Sharable
public class ServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel channel) {
        Scribbleshare.setLogger(channel).info("Connection opened");

        channel.pipeline().addLast(new ChannelDuplexHandler() {
                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
                        Scribbleshare.getLogger(ctx).log(Level.INFO, "Uncaught exception", throwable);
                    }
                });

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
