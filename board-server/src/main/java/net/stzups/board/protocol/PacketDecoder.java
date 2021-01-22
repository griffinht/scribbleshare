package net.stzups.board.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import net.stzups.board.protocol.client.ClientPacket;
import net.stzups.board.protocol.client.ClientPacketCreateDocument;
import net.stzups.board.protocol.client.ClientPacketDraw;
import net.stzups.board.protocol.client.ClientPacketOpenDocument;
import net.stzups.board.protocol.client.ClientPacketType;

import javax.naming.OperationNotSupportedException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Decodes a WebSocketFrame sent by the client to a ClientPacket
 */
@ChannelHandler.Sharable
public class PacketDecoder extends MessageToMessageDecoder<WebSocketFrame> {
    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame webSocketFrame, List<Object> list) throws Exception {
        if (webSocketFrame instanceof TextWebSocketFrame) {
            System.out.println(((TextWebSocketFrame) webSocketFrame).text());
        } else if (webSocketFrame instanceof BinaryWebSocketFrame) {
            ByteBuf byteBuf = webSocketFrame.content();
            ClientPacketType packetType = ClientPacketType.valueOf(byteBuf.readUnsignedByte());
            System.out.println("recv " + packetType);
            ClientPacket packet;
            switch (packetType) {
                case DRAW:
                    Point[] points = new Point[byteBuf.readUnsignedByte()];
                    for (int i = 0; i < points.length; i++) {
                        points[i] = new Point(byteBuf.readUnsignedByte(), byteBuf.readShort(), byteBuf.readShort());
                    }
                    packet = new ClientPacketDraw(points);
                    break;
                case OPEN_DOCUMENT:
                    packet = new ClientPacketOpenDocument(readString(byteBuf));
                    break;
                case CREATE_DOCUMENT:
                    packet = new ClientPacketCreateDocument();
                    break;
                default:
                    throw new OperationNotSupportedException("Unsupported packet type " + packetType+ " while decoding");
            }
            list.add(packet);
        }
    }

    private String readString(ByteBuf byteBuf) {
        byte[] buffer = new byte[byteBuf.readUnsignedByte()];
        byteBuf.readBytes(buffer);
        return new String(buffer, StandardCharsets.UTF_8);
    }
}
