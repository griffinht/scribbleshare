package net.stzups.board.room;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.stzups.board.Board;
import net.stzups.board.room.protocol.client.ClientPacket;
import net.stzups.board.room.protocol.client.ClientPacketOffsetDraw;
import net.stzups.board.room.protocol.server.ServerPacketDraw;

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
            case OFFSET_DRAW:
                ClientPacketOffsetDraw clientPacketOffsetDraw = (ClientPacketOffsetDraw) packet;
                room.sendPacketExcept(new ServerPacketDraw(client.getId(), clientPacketOffsetDraw.getOffsetX(), clientPacketOffsetDraw.getOffsetY()), client);
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
