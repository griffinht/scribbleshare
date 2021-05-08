package net.stzups.scribbleshare.room.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import net.stzups.scribbleshare.room.server.websocket.ClientMessageHandler;
import net.stzups.scribbleshare.room.server.websocket.protocol.ClientMessageDecoder;
import net.stzups.scribbleshare.room.server.websocket.protocol.ServerMessageEncoder;
import net.stzups.scribbleshare.server.ServerInitializer;

import javax.net.ssl.SSLException;

@ChannelHandler.Sharable
public class RoomServerInitializer extends ServerInitializer {
    public interface Config extends ServerInitializer.Config {
        String getWebsocketPath();
    }

    private final ServerMessageEncoder serverMessageEncoder = new ServerMessageEncoder();
    private final ClientMessageDecoder clientMessageDecoder = new ClientMessageDecoder();
    private final ClientMessageHandler clientMessageHandler = new ClientMessageHandler();

    private final Config config;

    public RoomServerInitializer(RoomServerInitializer.Config config) throws SSLException {
        super(config);
        this.config = config;
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        super.initChannel(channel);

        channel.pipeline()
                .addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(65536))
                .addLast(new WebSocketServerCompressionHandler())
                .addLast(new WebSocketServerProtocolHandler(config.getWebsocketPath(), null, true))
                .addLast(serverMessageEncoder)
                .addLast(clientMessageDecoder)
                .addLast(clientMessageHandler);//todo give this a different executor? https://stackoverflow.com/questions/49133447/how-can-you-safely-perform-blocking-operations-in-a-netty-channel-handler
    }
}
