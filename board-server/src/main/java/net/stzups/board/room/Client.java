package net.stzups.board.room;

import io.netty.channel.Channel;
import net.stzups.board.protocol.server.ServerPacket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Client {
    private User user;
    private Channel channel;

    private List<ServerPacket> packets = new ArrayList<>();

    Client(User user, Channel channel) {
        this.user = user;
        this.channel = channel;
    }

    public User getUser() {
        return user;
    }

    void queuePacket(ServerPacket serverPacket) {
        packets.add(serverPacket);
    }

    void sendPacket(ServerPacket serverPacket) {
        channel.writeAndFlush(Collections.singletonList(serverPacket));
    }

    void flushPackets() {
        if (packets.size() > 0) {
            channel.writeAndFlush(packets);
            packets = new ArrayList<>();
        }
    }

    @Override
    public String toString() {
        return "Client{user=" + user + ",address=" + channel.remoteAddress() + "}";
    }
}
