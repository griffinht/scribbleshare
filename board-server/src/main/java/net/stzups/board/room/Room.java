package net.stzups.board.room;

import io.netty.util.collection.LongObjectHashMap;
import net.stzups.board.Board;
import net.stzups.board.protocol.server.ServerPacket;
import net.stzups.board.protocol.server.ServerPacketAddUser;
import net.stzups.board.protocol.server.ServerPacketOpenDocument;
import net.stzups.board.protocol.server.ServerPacketRemoveUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

class Room {
    private static final int SEND_PERIOD = 1000;

    private static List<Room> rooms = new ArrayList<>();
    static {//todo send some packets instantly and refactor to somewhere?
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (Room room : rooms) {
                    for (Client client : room.clients.values()) {
                        client.flushPackets();
                    }
                }
            }
        }, 0, SEND_PERIOD);
    }

    private Map<Long, Client> clients = new LongObjectHashMap<>(); //probably faster with smaller memory footprint for long keys

    private Document document;
    private Room(Document document) {
        this.document = document;
    }

    /**
     * Creates a new room with a random id
     *
     * @return the created room
     */
    static Room createRoom(Document document) {
        Room room = new Room(document);
        rooms.add(room);
        return room;
    }

    Document getDocument() {
        return document;
    }

    /**
     * Creates a new client using its channel
     * todo
     */
    void addClient(Client client) {
        //for the new client
        client.sendPacket(new ServerPacketOpenDocument(document));
        //for the existing clients
        sendPacket(new ServerPacketAddUser(client.getUser()));
        clients.put(client.getUser().getId(), client);
        Board.getLogger().info("Added " + client + " to " + this);
    }

    /**
     * Removes given client from room
     *
     * @param client client to remove
     */
    void removeClient(Client client) {
        sendPacket(new ServerPacketRemoveUser(clients.remove(client.getUser().getId()).getUser()));
        Board.getLogger().info("Removed " + client + " to " + this);
    }

    /**
     * Send given packet to all members of the room except for the specified client
     *
     * @param serverPacket packet to send
     * @param except client to exclude
     */
    void sendPacketExcept(ServerPacket serverPacket, Client except) {
        for (Client client : clients.values()) {
            if (except != client) {
                client.sendPacket(serverPacket);
            }
        }
    }

    /**
     * Send given packet to all clients of this room
     *
     * @param serverPacket the packet to send
     */
    void sendPacket(ServerPacket serverPacket) {
        for (Client client : clients.values()) {
            client.sendPacket(serverPacket);
        }
    }

    @Override
    public String toString() {
        return "Room{document=" + document + "}";
    }
}
