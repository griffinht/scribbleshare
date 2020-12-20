package net.stzups.board.room;

import io.netty.channel.Channel;

public class Client {
    private int id;
    private Channel channel;
    private int x = 0;
    private int y = 0;

    Client(int id, Channel channel) {
        this.id = id;
        this.channel = channel;
    }

    public int getId() {
        return id;
    }

    void updatePosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }

    Channel getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "Client{id=" + id + ",address=" + channel.remoteAddress() + "}";
    }
}
