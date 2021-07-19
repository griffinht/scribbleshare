package net.stzups.scribbleshare.room.server.websocket.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.data.objects.exceptions.DeserializationException;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessageType;

import java.util.List;
import java.util.logging.Level;

/**
 * Decodes a WebSocketFrame sent by the client to a ClientPacket
 */
@ChannelHandler.Sharable
public class ClientMessageDecoder extends MessageToMessageDecoder<WebSocketFrame> {
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Scribbleshare.getLogger(ctx).log(Level.WARNING, "Exception while decoding WebSocketFrame to ClientMessage", cause);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame webSocketFrame, List<Object> list) throws DeserializationException {
        if (webSocketFrame instanceof TextWebSocketFrame) {
            Scribbleshare.getLogger(ctx).warning("Got TextWebSocketFrame, content:");//debug
            Scribbleshare.getLogger(ctx).warning(((TextWebSocketFrame) webSocketFrame).text());//debug
        } else if (webSocketFrame instanceof BinaryWebSocketFrame) {
            ByteBuf byteBuf = webSocketFrame.content();
            StringBuilder string = new StringBuilder("recv ");//debug
            while (byteBuf.isReadable()) {
                ClientMessageType clientMessageType = ClientMessageType.deserialize(byteBuf.readUnsignedByte());
                ClientMessage clientMessage = ClientMessage.getClientMessage(clientMessageType, byteBuf);
                list.add(clientMessage);
                string.append(clientMessage.getClass().getSimpleName()).append(", ");//debug
            }
            Scribbleshare.getLogger(ctx).info(string.toString());//debug
        }
    }
}
