package net.stzups.board.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import net.stzups.board.Board;
import net.stzups.board.room.PacketHandler;
import net.stzups.board.protocol.PacketEncoder;
import net.stzups.board.protocol.PacketDecoder;

/**
 * Creates pipeline to handle HTTP requests and WebSocket connections on the same port
 * WebSocket connections should be made to the specified WebSocket path
 * Connections not made to the WebSocket path go to ServerHandler
 */
public class ServerInitializer extends ChannelInitializer<SocketChannel> {
    private PacketEncoder packetEncoder = new PacketEncoder();
    private PacketDecoder packetDecoder = new PacketDecoder();

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        Board.getLogger().info("New connection from " + socketChannel.remoteAddress());
        ChannelPipeline pipeline = socketChannel.pipeline();
        //todo ssl
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new WebSocketServerCompressionHandler());
        pipeline.addLast(new WebSocketServerProtocolHandler("/websocket", null, true));
        pipeline.addLast(new HttpServerHandler());
        pipeline.addLast(packetEncoder);
        pipeline.addLast(packetDecoder);
        pipeline.addLast(new PacketHandler());
    }
}
