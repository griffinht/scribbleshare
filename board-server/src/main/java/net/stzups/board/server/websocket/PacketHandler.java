package net.stzups.board.server.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.stzups.board.Board;
import net.stzups.board.data.objects.Document;
import net.stzups.board.data.objects.User;
import net.stzups.board.data.objects.UserSession;
import net.stzups.board.server.websocket.protocol.client.ClientPacket;
import net.stzups.board.server.websocket.protocol.client.ClientPacketCreateDocument;
import net.stzups.board.server.websocket.protocol.client.ClientPacketDraw;
import net.stzups.board.server.websocket.protocol.client.ClientPacketHandshake;
import net.stzups.board.server.websocket.protocol.client.ClientPacketOpenDocument;
import net.stzups.board.server.websocket.protocol.server.ServerPacketAddDocument;
import net.stzups.board.server.websocket.protocol.server.ServerPacketDraw;
import net.stzups.board.server.WebSocketInitializer;
import net.stzups.board.server.websocket.protocol.server.ServerPacketHandshake;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class PacketHandler extends SimpleChannelInboundHandler<ClientPacket> {
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
        System.out.println(ctx.channel().hasAttr(WebSocketInitializer.HTTP_SESSION_KEY));
        System.out.println(ctx.channel().attr(WebSocketInitializer.HTTP_SESSION_KEY).get());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ClientPacket packet) {
        switch (packet.getPacketType()) {
            case DRAW: {
                ClientPacketDraw clientPacketDraw = (ClientPacketDraw) packet;
                room.getDocument().addPoints(client.getUser(), clientPacketDraw.getPoints());
                room.queuePacketExcept(new ServerPacketDraw(client.getUser(), clientPacketDraw.getPoints()), client);//todo this has tons of latency
                break;
            }
            case OPEN_DOCUMENT: {
                ClientPacketOpenDocument clientPacketOpenDocument = (ClientPacketOpenDocument) packet;
                Document document = Board.getDocument(clientPacketOpenDocument.getId());
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
                ClientPacketCreateDocument clientPacketCreateDocument = (ClientPacketCreateDocument) packet;
                if (room != null) {
                    room.removeClient(client);
                }
                try {
                    room = getRoom(Board.createDocument(client.getUser()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                client.sendPacket(new ServerPacketAddDocument(room.getDocument()));
                room.addClient(client);
                break;
            }
            case HANDSHAKE: {
                ClientPacketHandshake clientPacketHandshake = (ClientPacketHandshake) packet;
                if (client == null) {
                    if (clientPacketHandshake.getToken() == 0) {
                        System.out.println("user authed with empty session");
                        client = createUserSession(ctx, null);
                    } else {
                        UserSession userSession = Board.removeUserSession(clientPacketHandshake.getToken());
                        if (userSession == null) {
                            System.out.println("user tried authenticating with nonexistant session");
                            client = createUserSession(ctx, null);
                        } else if (!userSession.validate(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress())) {
                            System.out.println("user tried authenticating with invalid session" + userSession);
                            client = createUserSession(ctx, null);
                        } else {
                            System.out.println("good user session");
                            User user = Board.getUser(userSession.getUserId());
                            if (user == null) {
                                System.out.println("very bad user does not exist");
                            }
                            client = createUserSession(ctx, user);
                        }
                    }
                }
                if (client.getUser().getOwnedDocuments().size() == 0) {
                    client.queuePacket(new ServerPacketAddDocument(Board.createDocument(client.getUser())));
                } else {
                    for (long id : client.getUser().getOwnedDocuments()) {
                        client.queuePacket(new ServerPacketAddDocument(Board.getDocument(id)));
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
            Board.addUser(client.getUser());
        } else {
            client = new Client(user, ctx.channel());
        }
        UserSession userSession = new UserSession(client.getUser(), ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress());
        Board.addUserSession(userSession);
        client.queuePacket(new ServerPacketHandshake(userSession.getToken()));
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
