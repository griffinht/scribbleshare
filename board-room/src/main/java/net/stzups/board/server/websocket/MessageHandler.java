package net.stzups.board.server.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.stzups.board.BoardRoom;
import net.stzups.board.data.objects.Document;
import net.stzups.board.data.objects.User;
import net.stzups.board.data.objects.PersistentUserSession;
import net.stzups.board.server.websocket.protocol.client.ClientMessage;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageCreateDocument;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageHandshake;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageOpenDocument;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageUpdateCanvas;
import net.stzups.board.server.websocket.protocol.server.messages.ServerMessageAddDocument;
import net.stzups.board.server.websocket.protocol.server.messages.ServerMessageAddUser;
import net.stzups.board.server.websocket.protocol.server.messages.ServerMessageHandshake;

import java.util.HashMap;
import java.util.Map;

public class MessageHandler extends SimpleChannelInboundHandler<ClientMessage> {
    private static Map<Document, Room> documents = new HashMap<>();
    private Room room;
    private Client client;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (room != null) {
            room.removeClient(client);
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        //System.out.println(ctx.channel().hasAttr(WebSocketInitializer.HTTP_SESSION_KEY));
        //System.out.println(ctx.channel().attr(WebSocketInitializer.HTTP_SESSION_KEY).get());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ClientMessage message) {
        switch (message.getMessageType()) {
            case UPDATE_CANVAS: {
                room.updateClient(client, ((ClientMessageUpdateCanvas) message).getCanvasObjects());
                break;
            }
            case OPEN_DOCUMENT: {
                ClientMessageOpenDocument clientPacketOpenDocument = (ClientMessageOpenDocument) message;
                Document document = BoardRoom.getDatabase().getDocument(clientPacketOpenDocument.getId());
                if (document != null) {
                    if (room != null) {
                        room.removeClient(client);
                    }
                    room = getRoom(document);
                    room.addClient(client);
                } else {
                    System.out.println(client + " tried to open document not that does not exist");
                }
                break;
            }
            case CREATE_DOCUMENT: {
                ClientMessageCreateDocument clientPacketCreateDocument = (ClientMessageCreateDocument) message;
                if (room != null) {
                    room.removeClient(client);
                }
                try {
                    room = getRoom(BoardRoom.getDatabase().createDocument(client.getUser()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                client.sendMessage(new ServerMessageAddDocument(room.getDocument()));
                room.addClient(client);
                break;
            }
            case HANDSHAKE: {
                ClientMessageHandshake clientPacketHandshake = (ClientMessageHandshake) message;
                if (client == null) {
                    if (clientPacketHandshake.getToken() == 0) {
                        System.out.println("user authed with empty session");
                        client = createUserSession(ctx, null);
                    } else {
                        PersistentUserSession persistentUserSession = BoardRoom.getDatabase().removeUserSession(clientPacketHandshake.getId());
                        if (persistentUserSession == null) {
                            BoardRoom.getLogger().warning(ctx.channel().remoteAddress() + " attempted to authenticate with non existent persistent user session");
                            client = createUserSession(ctx, null);
                        } else if (!persistentUserSession.validate(clientPacketHandshake.getToken())) {
                            BoardRoom.getLogger().warning(ctx.channel().remoteAddress() + " attempted to authenticate with invalid persistent user session " + persistentUserSession);
                            client = createUserSession(ctx, null);
                        } else {
                            User user = BoardRoom.getDatabase().getUser(persistentUserSession.getUser());
                            if (user == null) {
                                BoardRoom.getLogger().severe(ctx.channel().remoteAddress() + " somehow managed to authenticate with non existent user");
                                return;
                            }
                            client = createUserSession(ctx, user);
                        }
                    }
                }
                client.queueMessage(new ServerMessageAddUser(client.getUser()));
                if (client.getUser().getOwnedDocuments().size() == 0) {
                    client.queueMessage(new ServerMessageAddDocument(BoardRoom.getDatabase().createDocument(client.getUser())));
                } else {
                    for (long id : client.getUser().getOwnedDocuments()) {
                        client.queueMessage(new ServerMessageAddDocument(BoardRoom.getDatabase().getDocument(id)));
                    }
                }
                client.flushMessages();

                break;
            }
            default:
                throw new UnsupportedOperationException("Unsupported message type " + message.getMessageType() + " sent by " + client);
        }
    }

    private static Client createUserSession(ChannelHandlerContext ctx, User user) {
        Client client;
        if (user == null) {
            client = new Client(new User(), ctx.channel());
            BoardRoom.getDatabase().addUser(client.getUser());
        } else {
            client = new Client(user, ctx.channel());
        }
        PersistentUserSession persistentUserSession = new PersistentUserSession(client.getUser());
        client.queueMessage(new ServerMessageHandshake(persistentUserSession));
        BoardRoom.getDatabase().addUserSession(persistentUserSession);
        return client;
    }

    /**
     * Gets or creates a room for an existing document
     *
     * @param document the existing document
     * @return the live room
     */
    private static Room getRoom(Document document) {
        Room r = documents.get(document);
        if (r == null) {
            r =  Room.startRoom(document);
            documents.put(r.getDocument(), r);
        }
        return r;
    }
}
