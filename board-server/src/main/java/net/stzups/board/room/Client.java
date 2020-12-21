package net.stzups.board.room;

import io.netty.channel.Channel;
import net.stzups.board.protocol.Point;
import net.stzups.board.protocol.server.ServerPacket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Client {
    private int id;
    private Channel channel;
    private List<Point> points = new ArrayList<>();
    private List<ServerPacket> packets = new ArrayList<>();

    Client(int id, Channel channel) {
        this.id = id;
        this.channel = channel;
    }

    public int getId() {
        return id;
    }

    void addPoints(Point[] points) {
        this.points.addAll(Arrays.asList(points));
    }

    List<Point> getPoints() {
        return points;
    }

    void addPacket(ServerPacket serverPacket) {
        packets.add(serverPacket);
    }

    void sendPackets() {
        if (packets.size() > 0) {
            channel.writeAndFlush(packets);
            packets = new ArrayList<>();
        }
    }

    @Override
    public String toString() {
        return "Client{id=" + id + ",address=" + channel.remoteAddress() + "}";
    }
}
