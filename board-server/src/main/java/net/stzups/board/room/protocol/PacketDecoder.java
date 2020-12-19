package net.stzups.board.room.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import net.stzups.board.room.protocol.packets.PacketDraw;
import net.stzups.board.room.protocol.packets.PacketOffsetDraw;

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
            PacketType packetType = PacketType.valueOf(byteBuf.readUnsignedByte());
            switch (packetType) {
                case DRAW:
                    list.add(new PacketDraw(byteBuf.readShort(), byteBuf.readShort()));
                    break;
                case OFFSET_DRAW:
                    list.add(new PacketOffsetDraw(byteBuf.readShort(), byteBuf.readShort()));
                    break;
                default:
                    throw new OperationNotSupportedException("Unsupported packet type " + packetType+ " while decoding");
            }
        }
    }
}
