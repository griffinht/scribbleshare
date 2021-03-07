package net.stzups.board.server.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.stzups.board.BoardRoom;
import net.stzups.board.data.objects.Document;
import net.stzups.board.data.objects.User;
import net.stzups.board.data.objects.UserSession;
import net.stzups.board.server.websocket.protocol.client.ClientMessage;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageCreateDocument;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageDraw;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageHandshake;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageOpenDocument;
import net.stzups.board.server.websocket.protocol.server.messages.ServerMessageAddDocument;
import net.stzups.board.server.websocket.protocol.server.messages.ServerMessageAddUser;
import net.stzups.board.server.websocket.protocol.server.messages.ServerMessageDrawClient;
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
    protected void channelRead0(ChannelHandlerContext ctx, ClientMessage packet) {
        switch (packet.getPacketType()) {
            case DRAW: {
                ClientMessageDraw clientPacketDraw = (ClientMessageDraw) packet;
                room.getDocument().addPoints(client.getUser(), clientPacketDraw.getPoints());
                room.queuePacketExcept(new ServerMessageDrawClient(client, clientPacketDraw.getPoints()), client);//todo this has tons of latency
                break;
            }
            case OPEN_DOCUMENT: {
                ClientMessageOpenDocument clientPacketOpenDocument = (ClientMessageOpenDocument) packet;
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
                ClientMessageCreateDocument clientPacketCreateDocument = (ClientMessageCreateDocument) packet;
                if (room != null) {
                    room.removeClient(client);
                }
                try {
                    room = getRoom(BoardRoom.getDatabase().createDocument(client.getUser()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                client.sendPacket(new ServerMessageAddDocument(room.getDocument()));
                room.addClient(client);
                break;
            }
            case HANDSHAKE: {
                ClientMessageHandshake clientPacketHandshake = (ClientMessageHandshake) packet;
                if (client == null) {
                    if (clientPacketHandshake.getToken() == 0) {
                        System.out.println("user authed with empty session");
                        client = createUserSession(ctx, null);
                    } else {
                        UserSession userSession = BoardRoom.getDatabase().removeUserSession(clientPacketHandshake.getToken());
                        if (userSession == null) {
                            System.out.println("user tried authenticating with nonexistant session");
                            client = createUserSession(ctx, null);
                        } else if (!userSession.validate(0)) {
                            System.out.println("user tried authenticating with invalid session" + userSession);
                            client = createUserSession(ctx, null);
                        } else {
                            System.out.println("good user session");
                            User user = BoardRoom.getDatabase().getUser(userSession.getUserId());
                            if (user == null) {
                                System.out.println("very bad user does not exist");
                            }
                            client = createUserSession(ctx, user);
                        }
                    }
                }
                client.queuePacket(new ServerMessageAddUser(client.getUser()));
                if (client.getUser().getOwnedDocuments().size() == 0) {
                    client.queuePacket(new ServerMessageAddDocument(BoardRoom.getDatabase().createDocument(client.getUser())));
                } else {
                    for (long id : client.getUser().getOwnedDocuments()) {
                        client.queuePacket(new ServerMessageAddDocument(BoardRoom.getDatabase().getDocument(id)));
                    }
                }
                client.flushPackets();

                break;
            }
            default:
                throw new UnsupportedOperationException("Unsupported packet type " + packet.getPacketType() + " sent by " + client);
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
        UserSession userSession = new UserSession(client.getUser(), 0);
        BoardRoom.getDatabase().addUserSession(userSession);
        client.queuePacket(new ServerMessageHandshake(userSession));
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
            r =  Room.createRoom(document);
            documents.put(r.getDocument(), r);
        }
        return r;
    }
}
