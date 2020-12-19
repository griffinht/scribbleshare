package net.stzups.board.room;

import io.netty.channel.Channel;
import io.netty.util.collection.IntObjectHashMap;
import net.stzups.board.room.protocol.packets.Packet;

import java.util.HashMap;
import java.util.Map;

class Room {
    private static final int ROOM_ID_LENGTH = 4;

    private static Map<String, Room> rooms = new HashMap<>();

    private int nextClientId = 0;

    private Map<Integer, Client> clients = new IntObjectHashMap<>(); //probably faster for int keys
    private String id;

    private Room(String id) {
        this.id = id;
    }

    /**
     * creates a new room with a random id
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
     * @return the newly created or existing room
     */
    static Room getRoom() {
        Room room;
        if (rooms.size() == 0) {
            return createRoom();
        } else {
            return rooms.values().iterator().next();
        }
    }

    String getId() {
        return id;
    }

    Client addClient(Channel channel) {
        Client client = new Client(nextClientId++, channel);
        clients.put(client.getId(), client);
        return client;
    }

    /**
     * Send given packet to all members of the room except for the specified client
     * @param packet packet to send
     * @param except client to exclude
     */
    void sendPacketExcept(Packet packet, Client except) {
        int a = 0;
        for (Client client : clients.values()) {
            if (except != client) {
                a++;
                client.getChannel().write(packet);
            }
        }
        System.out.println("sent to " + a + " except " + except);
    }
}
