package net.stzups.scribbleshare.room.server.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Client {
    private static final Random random = new Random();

    private final User user;
    private final Channel channel;
    private short id;

    private List<ServerMessage> messages = new ArrayList<>();

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
        id = (short) random.nextInt(); //todo is this cast less random
        if (id == 0) {//indicates fake client, should not be used by real clients
            return regenerateId();
        } else {
            return id;
        }
    }

    void queueMessage(ServerMessage serverMessage) {
        messages.add(serverMessage);
    }

    void sendMessage(ServerMessage serverMessage) {
        channel.writeAndFlush(Collections.singletonList(serverMessage));
    }

    void flushMessages() {
        if (messages.size() > 0) {
            channel.writeAndFlush(messages);
            messages = new ArrayList<>();//clear() won't work here as the above line does not block and writes later
        }
    }

    void disconnect() {
        try {
            channel.close().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeShort(id);
        byteBuf.writeLong(user.getId());
    }

    @Override
    public String toString() {
        return "Client{user=" + user + ",address=" + channel.remoteAddress() + "}";
    }

    @Override
    public int hashCode() {
        return id;
    }
}
