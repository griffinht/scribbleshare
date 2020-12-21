package net.stzups.board.room;

import net.stzups.board.protocol.server.ServerPacket;

public class EmptyClient extends Client {
    EmptyClient(int id) {
        super(id, null);
    }

    @Override
    void addPacket(ServerPacket serverPacket) {
    }

    @Override
    void sendPackets() {
    }

    @Override
    public String toString() {
        return "Fake" + super.toString();
    }
}
