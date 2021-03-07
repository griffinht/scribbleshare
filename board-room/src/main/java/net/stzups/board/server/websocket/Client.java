package net.stzups.board.server.websocket;

import io.netty.channel.Channel;
import net.stzups.board.BoardRoom;
import net.stzups.board.data.objects.User;
import net.stzups.board.server.websocket.protocol.server.ServerPacket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Client {
    private User user;
    private Channel channel;
    private short id;

    private List<ServerPacket> packets = new ArrayList<>();

    Client(User user, Channel channel) {
        this.user = user;
        this.channel = channel;
        regenerateId();
    }

    public User getUser() {
        return user;
    }

    public short getId() {
        return id;
    }

    public short regenerateId() {
        id = (short) BoardRoom.getRandom().nextInt(); //todo is this cast less random
        if (id == 0) {//indicates fake client, should not be used by real clients
            return regenerateId();
        } else {
            return id;
        }
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

    void disconnect() {
        try {
            channel.close().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "Client{user=" + user + ",address=" + channel.remoteAddress() + "}";
    }
}
