package net.stzups.scribbleshare.backend.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.stream.ChunkedWriteHandler;
import net.stzups.scribbleshare.backend.ScribbleshareBackendConfig;
import net.stzups.scribbleshare.backend.server.http.HttpServerHandler;
import net.stzups.scribbleshare.data.database.ScribbleshareDatabase;
import net.stzups.scribbleshare.server.http.HttpServerInitializer;

import javax.net.ssl.SSLException;

@ChannelHandler.Sharable
public class BackendHttpServerInitializer extends HttpServerInitializer {
    private final HttpServerHandler httpServerHandler;

    public BackendHttpServerInitializer(ScribbleshareBackendConfig config, ScribbleshareDatabase database) throws SSLException {
        super(config);
        httpServerHandler = new HttpServerHandler(config, database);
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        super.initChannel(channel);

        channel.pipeline()
                .addLast(new HttpContentCompressor())
                .addLast(new ChunkedWriteHandler())
                .addLast(httpServerHandler)
                .addLast(httpExceptionHandler());
    }
}
