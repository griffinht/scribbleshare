package net.stzups.board.room.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import net.stzups.board.room.protocol.client.ClientPacket;
import net.stzups.board.room.protocol.client.ClientPacketDraw;
import net.stzups.board.room.protocol.client.ClientPacketOffsetDraw;
import net.stzups.board.room.protocol.client.ClientPacketOpen;
import net.stzups.board.room.protocol.client.ClientPacketType;

import javax.naming.OperationNotSupportedException;
import java.util.List;

@ChannelHandler.Sharable
public class PacketDecoder extends MessageToMessageDecoder<WebSocketFrame> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, WebSocketFrame webSocketFrame, List<Object> list) throws Exception {
        if (webSocketFrame instanceof TextWebSocketFrame) {
            System.out.println(((TextWebSocketFrame) webSocketFrame).text());
        } else if (webSocketFrame instanceof BinaryWebSocketFrame) {
            ByteBuf byteBuf = webSocketFrame.content();
            ClientPacketType packetType = ClientPacketType.valueOf(byteBuf.readUnsignedByte());
            System.out.println(packetType);
            ClientPacket packet;
            switch (packetType) {
                case DRAW:
                    packet = new ClientPacketDraw(byteBuf.readShort(), byteBuf.readShort());
                    break;
                case OFFSET_DRAW:
                    packet = new ClientPacketOffsetDraw(byteBuf.readShort(), byteBuf.readShort());
                    break;
                case OPEN:
                    packet = new ClientPacketOpen();
                    break;
                default:
                    throw new OperationNotSupportedException("Unsupported packet type " + packetType+ " while decoding");
            }
            list.add(packet);
        }
    }
}
