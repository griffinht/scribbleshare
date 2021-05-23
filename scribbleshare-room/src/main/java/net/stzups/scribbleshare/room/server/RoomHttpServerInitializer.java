package net.stzups.scribbleshare.room.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.util.AttributeKey;
import net.stzups.scribbleshare.data.database.ScribbleshareDatabase;
import net.stzups.scribbleshare.room.server.websocket.ClientMessageHandler;
import net.stzups.scribbleshare.room.server.websocket.protocol.ClientMessageDecoder;
import net.stzups.scribbleshare.room.server.websocket.protocol.ServerMessageEncoder;
import net.stzups.scribbleshare.server.http.HttpServerInitializer;
import net.stzups.scribbleshare.server.http.handlers.HttpAuthenticator;

import javax.net.ssl.SSLException;

@ChannelHandler.Sharable
public class RoomHttpServerInitializer extends HttpServerInitializer {
    public interface Config extends HttpServerInitializer.Config {
        String getWebsocketPath();
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
    private final HttpAuthenticator httpAuthenticator;


    public RoomHttpServerInitializer(Config config, ScribbleshareDatabase database) throws SSLException {
        super(config);
        this.config = config;
        this.database = database;
        clientMessageHandler = new ClientMessageHandler(database);
        httpAuthenticator = new HttpAuthenticator(database);
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        super.initChannel(channel);

        channel.attr(DATABASE).set(database);

        channel.pipeline()
                .addLast(httpAuthenticator)
                .addLast(httpExceptionHandler())
                .addLast(new WebSocketServerCompressionHandler())
                .addLast(new WebSocketServerProtocolHandler(config.getWebsocketPath(), null, true))
                .addLast(serverMessageEncoder)
                .addLast(clientMessageDecoder)
                .addLast(clientMessageHandler);//todo give this a different executor? https://stackoverflow.com/questions/49133447/how-can-you-safely-perform-blocking-operations-in-a-netty-channel-handler
    }
}
