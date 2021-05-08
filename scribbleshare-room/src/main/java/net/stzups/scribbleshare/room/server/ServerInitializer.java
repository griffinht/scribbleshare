package net.stzups.scribbleshare.room.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import net.stzups.scribbleshare.room.ScribbleshareRoomConfigKeys;
import net.stzups.scribbleshare.room.server.websocket.ClientMessageHandler;
import net.stzups.scribbleshare.room.server.websocket.protocol.ClientMessageDecoder;
import net.stzups.scribbleshare.room.server.websocket.protocol.ServerMessageEncoder;

import javax.net.ssl.SSLException;

@ChannelHandler.Sharable
public class ServerInitializer extends net.stzups.scribbleshare.server.ServerInitializer {
    private final HttpAuthenticator httpAuthenticator = new HttpAuthenticator();
    private final ServerMessageEncoder serverMessageEncoder = new ServerMessageEncoder();
    private final ClientMessageDecoder clientMessageDecoder = new ClientMessageDecoder();
    private final ClientMessageHandler clientMessageHandler = new ClientMessageHandler();

    public ServerInitializer() throws SSLException {}

    @Override
    protected void initChannel(SocketChannel channel) {
        super.initChannel(channel);

        channel.pipeline()
                .addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(65536))
                .addLast(httpAuthenticator)
                .addLast(new WebSocketServerCompressionHandler())
                .addLast(new WebSocketServerProtocolHandler(net.stzups.scribbleshare.Scribbleshare.getConfig().getString(ScribbleshareRoomConfigKeys.WEBSOCKET_PATH), null, true))
                .addLast(serverMessageEncoder)
                .addLast(clientMessageDecoder)
                .addLast(clientMessageHandler);//todo give this a different executor? https://stackoverflow.com/questions/49133447/how-can-you-safely-perform-blocking-operations-in-a-netty-channel-handler
    }
}
