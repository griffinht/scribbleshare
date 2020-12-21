package net.stzups.board.room;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.stzups.board.Board;
import net.stzups.board.protocol.client.ClientPacket;
import net.stzups.board.protocol.client.ClientPacketDraw;
import net.stzups.board.protocol.server.ServerPacketDraw;

public class PacketHandler extends SimpleChannelInboundHandler<ClientPacket> {
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
            case DRAW:
                ClientPacketDraw clientPacketDraw = (ClientPacketDraw) packet;
                client.addPoints(clientPacketDraw.getPoints());
                room.sendPacketExcept(new ServerPacketDraw(client.getId(), clientPacketDraw.getPoints()), client);
                break;
            case OPEN:
                if (room == null) {
                    room = Room.getRoom();
                    client = room.addClient(ctx.channel());
                } else {
                    Board.getLogger().warning(client + " tried to open a new room when it was already open");
                }
                break;
            default:
                throw new UnsupportedOperationException("Unsupported packet type " + packet.getPacketType() + " sent by " + client);
        }
    }
}
