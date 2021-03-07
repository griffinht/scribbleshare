package net.stzups.board.server.websocket.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import net.stzups.board.server.websocket.protocol.client.ClientMessage;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageCreateDocument;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageDraw;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageHandshake;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageOpenDocument;
import net.stzups.board.server.websocket.protocol.client.ClientMessageType;

import javax.naming.OperationNotSupportedException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Decodes a WebSocketFrame sent by the client to a ClientPacket
 */
@ChannelHandler.Sharable
public class MessageDecoder extends MessageToMessageDecoder<WebSocketFrame> {
    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame webSocketFrame, List<Object> list) throws Exception {
        if (webSocketFrame instanceof TextWebSocketFrame) {
            System.out.println(((TextWebSocketFrame) webSocketFrame).text());
        } else if (webSocketFrame instanceof BinaryWebSocketFrame) {
            ByteBuf byteBuf = webSocketFrame.content();
            ClientMessageType packetType = ClientMessageType.valueOf(byteBuf.readUnsignedByte());
            System.out.println("recv " + packetType);
            ClientMessage message;
            switch (packetType) {
                case DRAW:
                    message = new ClientMessageDraw(byteBuf);
                    break;
                case OPEN_DOCUMENT:
                    message = new ClientMessageOpenDocument(byteBuf);
                    break;
                case CREATE_DOCUMENT:
                    message = new ClientMessageCreateDocument();
                    break;
                case HANDSHAKE:
                    message = new ClientMessageHandshake(byteBuf);
                    break;
                default:
                    throw new OperationNotSupportedException("Unsupported message type " + packetType + " while decoding");
            }
            list.add(message);
        }
    }

    private String readString(ByteBuf byteBuf) {
        byte[] buffer = new byte[byteBuf.readUnsignedByte()];
        byteBuf.readBytes(buffer);
        return new String(buffer, StandardCharsets.UTF_8);
    }
}
