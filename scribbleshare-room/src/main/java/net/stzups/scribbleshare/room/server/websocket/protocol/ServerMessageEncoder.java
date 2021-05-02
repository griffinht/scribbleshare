package net.stzups.scribbleshare.room.server.websocket.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import net.stzups.scribbleshare.room.server.ServerInitializer;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessage;

import java.util.List;

/**
 * Encodes a ServerPacket sent by the server to
 */
@ChannelHandler.Sharable
public class ServerMessageEncoder extends MessageToByteEncoder<List<ServerMessage>> {
    @Override
    protected void encode(ChannelHandlerContext ctx, List<ServerMessage> serverMessages, ByteBuf out) {
        StringBuilder stringBuilder = new StringBuilder();//debug
        BinaryWebSocketFrame binaryWebSocketFrame = new BinaryWebSocketFrame();
        for (ServerMessage serverMessage : serverMessages) {
            stringBuilder.append(serverMessage.getClass().getSimpleName()).append(", ");//debug
            serverMessage.serialize(binaryWebSocketFrame.content());
        }
        ServerInitializer.getLogger(ctx).info("send " + stringBuilder);//debug
        out.writeBytes(binaryWebSocketFrame.content());
    }
}
