package net.stzups.board.room.server.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import net.stzups.board.data.objects.User;
import net.stzups.board.room.BoardRoom;
import net.stzups.board.room.server.HttpAuthenticator;
import net.stzups.board.room.server.ServerInitializer;
import net.stzups.board.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.board.room.server.websocket.protocol.client.messages.ClientMessageHandshake;

import java.util.logging.Logger;

public class WebSocketHandshakeHandler extends SimpleChannelInboundHandler<ClientMessage> {
    private Logger logger;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        logger = ctx.channel().attr(ServerInitializer.LOGGER).get();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ClientMessage message) {
        switch (message.getMessageType()) {
            case HANDSHAKE: {
                ClientMessageHandshake clientPacketHandshake = (ClientMessageHandshake) message;
                User user = BoardRoom.getDatabase().getUser(ctx.channel().attr(HttpAuthenticator.USER).get());
                logger.info("Handshake with invite " + clientPacketHandshake.getCode() + ", " + user);
                ctx.pipeline().remove(this);
                ctx.pipeline().addLast(new ClientHandler(new Client(user, ctx.channel()), BoardRoom.getDatabase().getInviteCode(clientPacketHandshake.getCode())));
                break;
            }
            default:
                throw new UnsupportedOperationException("Unsupported message type " + message.getMessageType());
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) {
        if (event instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            logger.info("WebSocket connection initialized");
            //todo give this a different executor https://stackoverflow.com/questions/49133447/how-can-you-safely-perform-blocking-operations-in-a-netty-channel-handler
        }
    }
}
