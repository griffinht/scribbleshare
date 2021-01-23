package net.stzups.board.room;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.stzups.board.protocol.client.ClientPacket;
import net.stzups.board.protocol.client.ClientPacketCreateDocument;
import net.stzups.board.protocol.client.ClientPacketDraw;
import net.stzups.board.protocol.client.ClientPacketOpenDocument;
import net.stzups.board.protocol.server.ServerPacketAddDocument;
import net.stzups.board.protocol.server.ServerPacketDraw;
import net.stzups.board.protocol.server.ServerPacketOpenDocument;

import java.util.HashMap;
import java.util.Map;

public class PacketHandler extends SimpleChannelInboundHandler<ClientPacket> {
    private static int TODO = 0;
    private static Map<Document, Room> documents = new HashMap<>();//todo move somewhere
    private Room room;
    private Client client;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (room != null) {
            room.removeClient(client);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        client = new Client(new User(), ctx.channel());
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
                Document document = Document.getDocument(clientPacketOpenDocument.getId());
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
                    room = getRoom(Document.createDocument(client.getUser()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                client.sendPacket(new ServerPacketAddDocument(room.getDocument()));
                room.addClient(client);
                break;
            }
            case HANDSHAKE: {
                for (Document document : Document.getDocuments()) {//todo if user has no documents then make a blank one
                    client.sendPacket(new ServerPacketAddDocument(document));//todo this should only send one web socket message down the pipe
                }
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
