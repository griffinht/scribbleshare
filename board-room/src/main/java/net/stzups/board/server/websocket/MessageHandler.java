package net.stzups.board.server.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.stzups.board.BoardRoom;
import net.stzups.board.data.objects.Document;
import net.stzups.board.data.objects.PersistentUserSession;
import net.stzups.board.data.objects.User;
import net.stzups.board.server.ServerInitializer;
import net.stzups.board.server.websocket.protocol.client.ClientMessage;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageCreateDocument;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageDeleteDocument;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageHandshake;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageOpenDocument;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageUpdateCanvas;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageUpdateDocument;
import net.stzups.board.server.websocket.protocol.server.messages.ServerMessageDeleteDocument;
import net.stzups.board.server.websocket.protocol.server.messages.ServerMessageUpdateDocument;
import net.stzups.board.server.websocket.protocol.server.messages.ServerMessageAddUser;
import net.stzups.board.server.websocket.protocol.server.messages.ServerMessageHandshake;

public class MessageHandler extends SimpleChannelInboundHandler<ClientMessage> {
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
                    room = Room.getRoom(document);
                    room.addClient(client);
                } else {
                    ctx.channel().attr(ServerInitializer.LOGGER).get().warning(client + " tried to open document not that does not exist");
                }
                break;
            }
            case CREATE_DOCUMENT: {
                ClientMessageCreateDocument clientPacketCreateDocument = (ClientMessageCreateDocument) message;
                if (room != null) {
                    room.removeClient(client);
                }
                try {
                    room = Room.getRoom(BoardRoom.getDatabase().createDocument(client.getUser()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                client.sendMessage(new ServerMessageUpdateDocument(room.getDocument()));
                room.addClient(client);
                break;
            }
            case HANDSHAKE: {
                ClientMessageHandshake clientPacketHandshake = (ClientMessageHandshake) message;
                if (client == null) {
                    if (clientPacketHandshake.getToken() == 0) {
                        ctx.channel().attr(ServerInitializer.LOGGER).get().info("Authenticated with blank session");
                        client = createUserSession(ctx, null);
                    } else {
                        PersistentUserSession persistentUserSession = BoardRoom.getDatabase().removeUserSession(clientPacketHandshake.getId());
                        if (persistentUserSession == null) {
                            ctx.channel().attr(ServerInitializer.LOGGER).get().warning("Attempted to authenticate with non existent persistent user session");
                            client = createUserSession(ctx, null);
                        } else if (!persistentUserSession.validate(clientPacketHandshake.getToken())) {
                            ctx.channel().attr(ServerInitializer.LOGGER).get().warning("Attempted to authenticate with invalid persistent user session " + persistentUserSession);
                            client = createUserSession(ctx, null);
                        } else {
                            User user = BoardRoom.getDatabase().getUser(persistentUserSession.getUser());
                            if (user == null) {
                                ctx.channel().attr(ServerInitializer.LOGGER).get().severe("Somehow managed to authenticate with non existent user");
                                client = createUserSession(ctx, null);
                            } else {
                                ctx.channel().attr(ServerInitializer.LOGGER).get().info(user + " authenticated with good persistent session");
                                client = createUserSession(ctx, user);
                            }
                        }
                    }
                }
                client.queueMessage(new ServerMessageAddUser(client.getUser()));
                if (client.getUser().getOwnedDocuments().length == 0) {
                    client.queueMessage(new ServerMessageUpdateDocument(BoardRoom.getDatabase().createDocument(client.getUser())));//todo
                } else {
                    for (long id : client.getUser().getOwnedDocuments()) {
                        client.queueMessage(new ServerMessageUpdateDocument(BoardRoom.getDatabase().getDocument(id)));//todo aggregate
                    }
                }
                client.flushMessages();

                break;
            }
            case DELETE_DOCUMENT: {
                ClientMessageDeleteDocument clientMessageDeleteDocument = (ClientMessageDeleteDocument) message;
                if (clientMessageDeleteDocument.id() == room.getDocument().getId()) {
                    System.out.println("document is delete ROOM");
                    room.sendMessage(new ServerMessageDeleteDocument(room.getDocument()));
                    room.end();
                    BoardRoom.getDatabase().deleteDocument(room.getDocument());
                    break;
                }
                Document document = BoardRoom.getDatabase().getDocument(clientMessageDeleteDocument.id());
                if (document == null) {
                    ctx.channel().attr(ServerInitializer.LOGGER).get().warning(client + " tried to delete document that does not exist");
                    break;
                }
                if (!document.getOwner().equals(client.getUser())) {
                    ctx.channel().attr(ServerInitializer.LOGGER).get().warning(client + " tried to delete document they do not own");
                    break;
                }
                System.out.println("document is delete");
                BoardRoom.getDatabase().deleteDocument(room.getDocument());
                //Room.getRoom(document);
                break;//todo better update logic
            }
            case UPDATE_DOCUMENT: {
                ClientMessageUpdateDocument clientMessageUpdateDocument = (ClientMessageUpdateDocument) message;
                if (clientMessageUpdateDocument.getName().length() > 64) {
                    ctx.channel().attr(ServerInitializer.LOGGER).get().warning(client + " tried to change name to string that is too long (" + clientMessageUpdateDocument.getName().length() + ")");
                    break;
                }
                room.getDocument().setName(clientMessageUpdateDocument.getName());
                BoardRoom.getDatabase().updateDocument(room.getDocument());
                break;//todo better update logic
            }
            default:
                throw new UnsupportedOperationException("Unsupported message type " + message.getMessageType() + " sent by " + client);
        }
    }

    private static Client createUserSession(ChannelHandlerContext ctx, User user) {
        Client client;
        if (user == null) {
            client = new Client(BoardRoom.getDatabase().createUser(), ctx.channel());
        } else {
            client = new Client(user, ctx.channel());
        }
        PersistentUserSession persistentUserSession = new PersistentUserSession(client.getUser());
        client.queueMessage(new ServerMessageHandshake(persistentUserSession));
        BoardRoom.getDatabase().addUserSession(persistentUserSession);
        return client;
    }
}
