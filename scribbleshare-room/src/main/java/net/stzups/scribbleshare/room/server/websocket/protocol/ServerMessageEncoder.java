package net.stzups.scribbleshare.room.server.websocket.protocol;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessage;

import java.util.List;

/**
 * Encodes a ServerPacket sent by the server to
 */
@ChannelHandler.Sharable
public class ServerMessageEncoder extends MessageToMessageDecoder<List<ServerMessage>> {
    @Override
    protected void decode(ChannelHandlerContext ctx, List<ServerMessage> serverMessages, List<Object> out) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();//debug
        BinaryWebSocketFrame binaryWebSocketFrame = new BinaryWebSocketFrame();
        for (ServerMessage serverMessage : serverMessages) {
            stringBuilder.append(serverMessage.getClass().getSimpleName()).append(", ");//debug
            serverMessage.serialize(binaryWebSocketFrame.content());
        }
        Scribbleshare.getLogger(ctx).info("send " + stringBuilder);//debug
        out.add(binaryWebSocketFrame);
    }
}
