package net.stzups.board.room;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.stzups.board.room.protocol.packets.Packet;

public class PacketHandler extends SimpleChannelInboundHandler<Packet> {
    private Room room;
    private Client client;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Packet packet) {
        if (room == null) {//todo do this when the client sends new client packet or something
            room = Room.getRoom();
            client = room.addClient(channelHandlerContext.channel());
        }
        switch (packet.getPacketType()) {
            case OFFSET_DRAW:
            case DRAW:
                room.sendPacketExcept(packet, client);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported packet type " + packet.getPacketType() + " sent by " + client);
        }
    }
}
