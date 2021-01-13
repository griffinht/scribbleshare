package net.stzups.board.room;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.stzups.board.protocol.client.ClientPacket;
import net.stzups.board.protocol.client.ClientPacketCreateDocument;
import net.stzups.board.protocol.client.ClientPacketDraw;
import net.stzups.board.protocol.client.ClientPacketOpenDocument;
import net.stzups.board.protocol.server.ServerPacketDraw;

import java.util.HashMap;
import java.util.Map;

public class PacketHandler extends SimpleChannelInboundHandler<ClientPacket> {
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
    protected void channelRead0(ChannelHandlerContext ctx, ClientPacket packet) {
        switch (packet.getPacketType()) {
            case DRAW: {
                ClientPacketDraw clientPacketDraw = (ClientPacketDraw) packet;
                client.addPoints(clientPacketDraw.getPoints());
                room.sendPacketExcept(new ServerPacketDraw(client.getId(), clientPacketDraw.getPoints()), client);
                break;
            }
            case OPEN_DOCUMENT: {
                ClientPacketOpenDocument clientPacketOpenDocument = (ClientPacketOpenDocument) packet;
                Document document = Document.getDocument(clientPacketOpenDocument.getId());
                if (document != null) {
                    Room r = documents.get(document);
                    if (r != null) {
                        room = r;
                    } else {
                        room = Room.createRoom(document);
                        documents.put(document, room);
                    }
                    client = room.addClient(ctx.channel());
                } else {
                    System.out.println("null");
                }
                break;
            }
            case CREATE_DOCUMENT: {
                ClientPacketCreateDocument clientPacketCreateDocument = (ClientPacketCreateDocument) packet;
                room = Room.createRoom(Document.createDocument(clientPacketCreateDocument.getName()));
                documents.put(room.getDocument(), room);
                client = room.addClient(ctx.channel());
                break;
            }
            default:
                throw new UnsupportedOperationException("Unsupported packet type " + packet.getPacketType() + " sent by " + client);
        }
    }
}
