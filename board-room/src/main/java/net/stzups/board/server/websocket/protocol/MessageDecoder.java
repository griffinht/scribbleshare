package net.stzups.board.server.websocket.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import net.stzups.board.server.ServerInitializer;
import net.stzups.board.server.websocket.protocol.client.ClientMessage;
import net.stzups.board.server.websocket.protocol.client.ClientMessageType;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageCreateDocument;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageDeleteDocument;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageHandshake;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageOpenDocument;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageUpdateCanvas;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageUpdateDocument;

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
            ctx.channel().attr(ServerInitializer.LOGGER).get().warning("Got TextWebSocketFrame, content:");
            ctx.channel().attr(ServerInitializer.LOGGER).get().warning(((TextWebSocketFrame) webSocketFrame).text());
        } else if (webSocketFrame instanceof BinaryWebSocketFrame) {
            ByteBuf byteBuf = webSocketFrame.content();
            ClientMessageType clientMessageType = ClientMessageType.valueOf(byteBuf.readUnsignedByte());
            ClientMessage clientMessage = ClientMessage.getClientMessage(clientMessageType, byteBuf);
            ctx.channel().attr(ServerInitializer.LOGGER).get().info("recv " + clientMessage.getClass().getSimpleName());
            list.add(clientMessage);
        }
    }
}
