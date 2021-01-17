package net.stzups.board.room;

import net.stzups.board.protocol.server.ServerPacket;

public class EmptyClient extends Client {
    EmptyClient(int id) {
        super(id, null);
    }

    @Override
    void sendPacket(ServerPacket serverPacket) {
    }

    @Override
    void flushPackets() {
    }

    @Override
    public String toString() {
        return "FakeClient{id=" + getId() + "}";
    }
}
