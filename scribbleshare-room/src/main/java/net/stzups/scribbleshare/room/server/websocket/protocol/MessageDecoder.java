package net.stzups.scribbleshare.room.server.websocket.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import net.stzups.scribbleshare.room.server.ServerInitializer;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessageType;

import java.util.List;

/**
 * Decodes a WebSocketFrame sent by the client to a ClientPacket
 */
@ChannelHandler.Sharable
public class MessageDecoder extends MessageToMessageDecoder<WebSocketFrame> {
    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame webSocketFrame, List<Object> list) {
        if (webSocketFrame instanceof TextWebSocketFrame) {
            ServerInitializer.getLogger(ctx).warning("Got TextWebSocketFrame, content:");//debug
            ServerInitializer.getLogger(ctx).warning(((TextWebSocketFrame) webSocketFrame).text());//debug
        } else if (webSocketFrame instanceof BinaryWebSocketFrame) {
            ByteBuf byteBuf = webSocketFrame.content();
            StringBuilder string = new StringBuilder("recv ");//debug
            while (byteBuf.isReadable()) {
                ClientMessageType clientMessageType = ClientMessageType.valueOf(byteBuf.readUnsignedByte());
                ClientMessage clientMessage = ClientMessage.getClientMessage(clientMessageType, byteBuf);
                list.add(clientMessage);
                string.append(clientMessage.getClass().getSimpleName()).append(", ");//debug
            }
            ServerInitializer.getLogger(ctx).info(string.toString());//debug
        }
    }
}
