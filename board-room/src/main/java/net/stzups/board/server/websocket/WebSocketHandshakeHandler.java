package net.stzups.board.server.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.stzups.board.BoardRoom;
import net.stzups.board.data.objects.PersistentUserSession;
import net.stzups.board.data.objects.User;
import net.stzups.board.server.ServerInitializer;
import net.stzups.board.server.websocket.protocol.client.ClientMessage;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageHandshake;
import net.stzups.board.server.websocket.protocol.server.messages.ServerMessageAddUser;
import net.stzups.board.server.websocket.protocol.server.messages.ServerMessageHandshake;

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
                Client client;
                if (clientPacketHandshake.getToken() == 0) {
                    logger.info("Authenticated with blank session");
                    client = createUserSession(ctx, null);
                } else {
                    PersistentUserSession persistentUserSession = BoardRoom.getDatabase().removeUserSession(clientPacketHandshake.getId());
                    if (persistentUserSession == null) {
                        logger.warning("Attempted to authenticate with non existent persistent user session");
                        client = createUserSession(ctx, null);
                    } else if (!persistentUserSession.validate(clientPacketHandshake.getToken())) {
                        logger.warning("Attempted to authenticate with invalid persistent user session " + persistentUserSession);
                        client = createUserSession(ctx, null);
                    } else {
                        User user = BoardRoom.getDatabase().getUser(persistentUserSession.getUser());
                        if (user == null) {
                            logger.severe("Somehow managed to authenticate with non existent user");
                            client = createUserSession(ctx, null);
                        } else {
                            logger.info(user + " authenticated with good persistent session");
                            client = createUserSession(ctx, user);
                        }
                    }
                }
                client.queueMessage(new ServerMessageAddUser(client.getUser()));
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
        PersistentUserSession persistentUserSession = new PersistentUserSession(client.getUser());//todo refactor
        client.queueMessage(new ServerMessageHandshake(persistentUserSession));
        BoardRoom.getDatabase().addUserSession(persistentUserSession);
        return client;
    }
}
