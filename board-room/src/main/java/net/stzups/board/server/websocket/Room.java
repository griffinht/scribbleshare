package net.stzups.board.server.websocket;

import net.stzups.board.BoardRoom;
import net.stzups.board.data.objects.Document;
import net.stzups.board.server.websocket.protocol.server.ServerMessage;
import net.stzups.board.server.websocket.protocol.server.messages.ServerMessageAddClient;
import net.stzups.board.server.websocket.protocol.server.messages.ServerMessageOpenDocument;
import net.stzups.board.server.websocket.protocol.server.messages.ServerMessageRemoveClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
                    for (Client client : room.clients) {
                        client.flushPackets();
                    }
                }
            }
        }, 0, SEND_PERIOD);
    }

    private Set<Client> clients = new HashSet<>();

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
     * Creates a new client using its channel.
     * todo
     */
    void addClient(Client client) {

        //for the new client
        client.sendPacket(new ServerMessageOpenDocument(document));
        //for the existing clients
        sendPacket(new ServerMessageAddClient(client));
        clients.add(client);
        BoardRoom.getLogger().info("Added " + client + " to " + this);
    }

    /**
     * Removes given client from room
     *
     * @param client client to remove
     */
    void removeClient(Client client) {
        clients.remove(client);
        sendPacket(new ServerMessageRemoveClient(client));
        BoardRoom.getLogger().info("Removed " + client + " to " + this);
    }

    /**
     * Send given packet to all members of the room except for the specified client
     *
     * @param serverMessage packet to send
     * @param except client to exclude
     */
    void sendPacketExcept(ServerMessage serverMessage, Client except) {
        for (Client client : clients) {
            if (except != client) {
                client.sendPacket(serverMessage);
            }
        }
    }

    /**
     * Send given packet to all clients of this room
     *
     * @param serverMessage the packet to send
     */
    void sendPacket(ServerMessage serverMessage) {
        for (Client client : clients) {
            client.sendPacket(serverMessage);
        }
    }

    void queuePacketExcept(ServerMessage serverMessage, Client except) {
        for (Client client : clients) {
            if (except != client) {
                client.queuePacket(serverMessage);
            }
        }
    }

    void queuePacket(ServerMessage serverMessage) {
        for (Client client : clients) {
            client.queuePacket(serverMessage);
        }
    }

    @Override
    public String toString() {
        return "Room{document=" + document + "}";
    }
}
