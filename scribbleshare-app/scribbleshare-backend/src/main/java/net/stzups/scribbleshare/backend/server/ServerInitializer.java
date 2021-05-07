package net.stzups.scribbleshare.backend.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import net.stzups.scribbleshare.backend.server.http.HttpServerHandler;

import javax.net.ssl.SSLException;

@ChannelHandler.Sharable
public class ServerInitializer extends net.stzups.scribbleshare.server.ServerInitializer {
   private final HttpServerHandler httpServerHandler = new HttpServerHandler();

    public ServerInitializer() throws SSLException {}

    @Override
    protected void initChannel(SocketChannel channel) {
        super.initChannel(channel);

        channel.pipeline().addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(Integer.MAX_VALUE)) //2gb todo decrease
                .addLast(new HttpContentCompressor())
                .addLast(new ChunkedWriteHandler())
                .addLast(httpServerHandler);
    }
}
