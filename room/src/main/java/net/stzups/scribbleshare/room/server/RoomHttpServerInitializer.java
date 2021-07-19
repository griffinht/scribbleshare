package net.stzups.scribbleshare.room.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.util.AttributeKey;
import net.stzups.netty.http.DefaultHttpServerHandler;
import net.stzups.netty.http.HttpServerHandler;
import net.stzups.netty.http.HttpServerInitializer;
import net.stzups.netty.http.exception.exceptions.NotFoundException;
import net.stzups.netty.http.handler.HttpHandler;
import net.stzups.netty.http.handler.handlers.OriginHandler;
import net.stzups.scribbleshare.data.database.ScribbleshareDatabase;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpConfig;
import net.stzups.scribbleshare.room.server.websocket.ClientMessageHandler;
import net.stzups.scribbleshare.room.server.websocket.protocol.ClientMessageDecoder;
import net.stzups.scribbleshare.room.server.websocket.protocol.ServerMessageEncoder;
import net.stzups.scribbleshare.server.http.handler.handlers.HttpAuthenticator;

import javax.net.ssl.SSLException;

@ChannelHandler.Sharable
public class RoomHttpServerInitializer extends HttpServerInitializer {
    public interface Config extends HttpServerInitializer.Config, HttpConfig {
        String getWebsocketPath();
        String getOrigin();
    }

    private static final AttributeKey<ScribbleshareDatabase> DATABASE = AttributeKey.valueOf(RoomHttpServerInitializer.class, "DATABASE");
    public static ScribbleshareDatabase getDatabase(ChannelHandlerContext ctx) {
        return ctx.channel().attr(DATABASE).get();
    }

    private final Config config;
    private final ScribbleshareDatabase database;

    private final ServerMessageEncoder serverMessageEncoder = new ServerMessageEncoder();
    private final ClientMessageDecoder clientMessageDecoder = new ClientMessageDecoder();
    private final ClientMessageHandler clientMessageHandler;
    private final HttpServerHandler httpServerHandler;


    public RoomHttpServerInitializer(Config config, ScribbleshareDatabase database) throws SSLException {
        super(config);
        this.config = config;
        this.database = database;
        clientMessageHandler = new ClientMessageHandler();
        httpServerHandler = new DefaultHttpServerHandler()
                .addLast(new OriginHandler(config))
                .addLast(new HttpHandler("/") {
                    @Override
                    public boolean handle(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponse response) throws NotFoundException {
                        if (!request.uri().equals(config.getWebsocketPath())) {
                            throw new NotFoundException("Bad uri " + request.uri());
                        }

                        return false;
                    }
                })
                .addLast(new HttpAuthenticator<>(database));
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        super.initChannel(channel);

        channel.attr(DATABASE).set(database);

        channel.pipeline()
                .addLast(httpServerHandler)
                .addLast(new WebSocketServerCompressionHandler())
                .addLast(new WebSocketServerProtocolHandler(config.getWebsocketPath(), null, true))
                .addLast(serverMessageEncoder)
                .addLast(clientMessageDecoder)
                .addLast(clientMessageHandler);//todo give this a different executor? https://stackoverflow.com/questions/49133447/how-can-you-safely-perform-blocking-operations-in-a-netty-channel-handler
    }
}
