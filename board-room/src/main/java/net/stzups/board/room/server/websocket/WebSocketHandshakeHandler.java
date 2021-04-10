package net.stzups.board.room.server.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import net.stzups.board.data.objects.session.PersistentHttpSession;
import net.stzups.board.data.objects.User;
import net.stzups.board.room.BoardRoom;
import net.stzups.board.room.server.ServerInitializer;
import net.stzups.board.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.board.room.server.websocket.protocol.client.messages.ClientMessageHandshake;
import net.stzups.board.room.server.websocket.protocol.server.messages.ServerMessageHandshake;

import java.util.logging.Logger;

public class WebSocketHandshakeHandler extends SimpleChannelInboundHandler<ClientMessage> {
    private Logger logger;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        logger = ctx.channel().attr(ServerInitializer.LOGGER).get();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ClientMessage message) {
        switch (message.getMessageType()) {//todo
            case HANDSHAKE: {
                ClientMessageHandshake clientPacketHandshake = (ClientMessageHandshake) message;
                Client client;
                if (clientPacketHandshake.getToken() == 0) {
                    logger.info("Authenticated with blank session");
                    client = createUserSession(ctx, null);
                } else {
                    PersistentHttpSession persistentHttpSession = BoardRoom.getDatabase().getAndRemovePersistentHttpSession(clientPacketHandshake.getId());
                    if (persistentHttpSession == null) {
                        logger.warning("Attempted to authenticate with non existent persistent user session");
                        client = createUserSession(ctx, null);
                    } else if (!persistentHttpSession.validate(null)) {
                        logger.warning("Attempted to authenticate with invalid persistent user session " + persistentHttpSession);
                        client = createUserSession(ctx, null);
                    } else {
                        User user = BoardRoom.getDatabase().getUser(persistentHttpSession.getUser());
                        if (user == null) {
                            logger.severe("Somehow managed to authenticate with non existent user");
                            client = createUserSession(ctx, null);
                        } else {
                            logger.info(user + " authenticated with good persistent session");
                            client = createUserSession(ctx, user);
                        }
                    }
                }
                ctx.pipeline().remove(this);
                ctx.pipeline().addLast(new ClientHandler(client, BoardRoom.getDatabase().getInviteCode(clientPacketHandshake.getCode())));
                break;
            }
            default:
                throw new UnsupportedOperationException("Unsupported message type " + message.getMessageType());
        }
    }

    private static Client createUserSession(ChannelHandlerContext ctx, User user) {
        Client client;
        if (user == null) {
            client = new Client(BoardRoom.getDatabase().createUser(), ctx.channel());
        } else {
            client = new Client(user, ctx.channel());
        }
        PersistentHttpSession persistentHttpSession = new PersistentHttpSession();//todo refactor
        client.queueMessage(new ServerMessageHandshake(persistentHttpSession));
        BoardRoom.getDatabase().addPersistentHttpSession(persistentHttpSession);
        return client;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) {
        if (event instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            logger.info("WebSocket connection initialized");
            //todo give this a different executor https://stackoverflow.com/questions/49133447/how-can-you-safely-perform-blocking-operations-in-a-netty-channel-handler
        }
    }
}
