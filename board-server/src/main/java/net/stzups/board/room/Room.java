package net.stzups.board.room;

import io.netty.channel.Channel;
import io.netty.util.collection.IntObjectHashMap;
import net.stzups.board.room.protocol.server.ServerPacket;
import net.stzups.board.room.protocol.server.ServerPacketAddClient;
import net.stzups.board.room.protocol.server.ServerPacketRemoveClient;

import java.util.HashMap;
import java.util.Map;

class Room {
    private static final int ROOM_ID_LENGTH = 4;

    private static Map<String, Room> rooms = new HashMap<>();

    private int nextClientId = 0;

    private Map<Integer, Client> clients = new IntObjectHashMap<>(); //probably faster with smaller memory footprint for int keys
    private String id;

    private Room(String id) {
        this.id = id;
    }

    /**
     * Creates a new room with a random id
     *
     * @return the created room
     */
    static Room createRoom() {
        String id = String.valueOf((int) (Math.random() * ROOM_ID_LENGTH));
        Room room = new Room(id);
        rooms.put(room.getId(), room);
        return room;
    }

    /**
     * for testing purposes, get the first room that already exists
     * if no rooms exist, one will be made
     *
     * @return the newly created or existing room
     */
    static Room getRoom() {
        if (rooms.size() == 0) {
            return createRoom();
        } else {
            return rooms.values().iterator().next();
        }
    }



    String getId() {
        return id;
    }

    /**
     * Creates a new client using its channel
     *
     * @param channel the channel used by the client
     * @return the newly created client
     */
    Client addClient(Channel channel) {
        Client client = new Client(nextClientId++, channel);
        sendPacket(new ServerPacketAddClient(client));
        clients.put(client.getId(), client);
        return client;
    }

    void removeClient(Client client) {
        clients.remove(client.getId());
        sendPacket(new ServerPacketRemoveClient(client));
    }
    /**
     * Send given packet to all members of the room except for the specified client
     *
     * @param serverPacket packet to send
     * @param except client to exclude
     */
    void sendPacketExcept(ServerPacket serverPacket, Client except) {
        int a = 0;
        System.out.print("not sending to " + except + ", sending to ");
        for (Client client : clients.values()) {
            if (except != client) {
                a++;
                System.out.print(client + " ");
                client.getChannel().write(serverPacket);
            }
        }
        System.out.println("(" + a + "/" + clients.size() + ")");
    }

    /**
     * Send packet to client
     *
     * @param serverPacket the packet to send
     * @param client the client to send to
     */
    void sendPacket(ServerPacket serverPacket, Client client) {
        client.getChannel().write(serverPacket);
    }

    /**
     * Send given packet to all clients of this room
     *
     * @param serverPacket the packet to send
     */
    void sendPacket(ServerPacket serverPacket) {
        for (Client client : clients.values()) {
            client.getChannel().write(serverPacket);
        }
    }
}
