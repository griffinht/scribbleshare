package net.stzups.scribbleshare.backend.server;

import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import net.stzups.scribbleshare.backend.server.http.HttpServerHandler;


/**
 * Creates pipeline to handle Websocket connections
 * WebSocket connections should be made to the specified WebSocket path
 * Connections not made to the WebSocket path go to ServerHandler
 */
public class ServerInitializer extends net.stzups.scribbleshare.server.ServerInitializer {
   private final HttpServerHandler httpServerHandler = new HttpServerHandler();

    ServerInitializer(SslContext sslContext) {
        super(sslContext);
    }

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
