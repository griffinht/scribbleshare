package net.stzups.board.server.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.stzups.board.Board;
import net.stzups.board.data.objects.Document;
import net.stzups.board.server.websocket.protocol.client.ClientPacket;
import net.stzups.board.server.websocket.protocol.client.ClientPacketCreateDocument;
import net.stzups.board.server.websocket.protocol.client.ClientPacketDraw;
import net.stzups.board.server.websocket.protocol.client.ClientPacketOpenDocument;
import net.stzups.board.server.websocket.protocol.server.ServerPacketAddDocument;
import net.stzups.board.server.websocket.protocol.server.ServerPacketDraw;
import net.stzups.board.server.WebSocketInitializer;

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
        client = new Client(Board.getUser(ctx.channel().attr(WebSocketInitializer.HTTP_SESSION_KEY).get()), ctx.channel());
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
                if (client.getUser().getOwnedDocuments().size() == 0) {
                    client.queuePacket(new ServerPacketAddDocument(Board.createDocument(client.getUser())));
                } else {
                    for (String id : client.getUser().getOwnedDocuments()) {
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
