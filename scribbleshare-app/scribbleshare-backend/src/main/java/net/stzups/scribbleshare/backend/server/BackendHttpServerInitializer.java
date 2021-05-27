package net.stzups.scribbleshare.backend.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.stream.ChunkedWriteHandler;
import net.stzups.scribbleshare.backend.ScribbleshareBackendConfig;
import net.stzups.scribbleshare.backend.server.http.handler.handlers.DocumentRequestHandler;
import net.stzups.scribbleshare.backend.server.http.handler.handlers.FileRequestHandler;
import net.stzups.scribbleshare.backend.server.http.handler.handlers.LoginFormHandler;
import net.stzups.scribbleshare.backend.server.http.handler.handlers.LogoutFormHandler;
import net.stzups.scribbleshare.backend.server.http.handler.handlers.RegisterFormHandler;
import net.stzups.scribbleshare.data.database.ScribbleshareDatabase;
import net.stzups.scribbleshare.server.http.HttpServerHandler;
import net.stzups.scribbleshare.server.http.HttpServerInitializer;
import net.stzups.scribbleshare.server.http.handler.handlers.HealthcheckRequestHandler;

import javax.net.ssl.SSLException;

@ChannelHandler.Sharable
public class BackendHttpServerInitializer extends HttpServerInitializer {
    private final HttpServerHandler httpServerHandler;

    public BackendHttpServerInitializer(ScribbleshareBackendConfig config, ScribbleshareDatabase database) throws SSLException {
        super(config);
        httpServerHandler = new HttpServerHandler()
                .addLast(new HealthcheckRequestHandler())
                .addLast(new DocumentRequestHandler(database))
                .addLast(new LoginFormHandler(config, database))
                .addLast(new LogoutFormHandler(config, database))
                .addLast(new RegisterFormHandler(database))
                .addLast(new FileRequestHandler(config));
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        super.initChannel(channel);

        channel.pipeline()
                .addLast(new HttpContentCompressor())
                .addLast(new ChunkedWriteHandler())
                .addLast(httpServerHandler);
    }
}
