package net.stzups.board.room;

import io.netty.channel.Channel;
import io.netty.util.collection.IntObjectHashMap;
import net.stzups.board.Board;
import net.stzups.board.protocol.Point;
import net.stzups.board.protocol.server.ServerPacket;
import net.stzups.board.protocol.server.ServerPacketAddClient;
import net.stzups.board.protocol.server.ServerPacketDraw;
import net.stzups.board.protocol.server.ServerPacketOpen;
import net.stzups.board.protocol.server.ServerPacketRemoveClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

class Room {
    private static final int SEND_PERIOD = 1000;
    private static final int ROOM_ID_LENGTH = 4;

    private static Map<String, Room> rooms = new HashMap<>();
    static {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (Room room : rooms.values()) {
                    for (Client client : room.clients.values()) {
                        client.sendPackets();
                    }
                }
            }
        }, 0, SEND_PERIOD);
    }

    private int nextClientId = 1; //0 is reserved for room client

    private Map<Integer, Client> clients = new IntObjectHashMap<>(); //probably faster with smaller memory footprint for int keys
    private Client emptyClient;
    private String id;

    private Room(String id) {
        this.id = id;
        emptyClient = new EmptyClient(0);
        clients.put(emptyClient.getId(), emptyClient);
    }

    /**
     * Creates a new room with a random id
     *
     * @return the created room
     */
    static Room createRoom() {
        String id;
        do {
            id = String.valueOf((int) (Math.random() * Math.pow(10, ROOM_ID_LENGTH)));
        } while (rooms.containsKey(id)); //todo improve
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
        //for the new client
        sendPacket(new ServerPacketOpen(id), client);
        for (Client c : clients.values()) {
            sendPacket(new ServerPacketAddClient(c), client);
            List<Point> points = c.getPoints();
            if (points.size() > 0) {
                sendPacket(new ServerPacketDraw(c.getId(), convert(new ArrayList<>(points))), client);
            }
        }
        //for the existing clients
        sendPacket(new ServerPacketAddClient(client));
        clients.put(client.getId(), client);
        Board.getLogger().info("Added " + client + " to " + this);
        return client;
    }

    /**
     * Converts a List<Point> to Point[], and marks them as instant draw
     * @param points points to convert
     * @return converted points
     */
    private static Point[] convert(List<Point> points) {
        Point[] pts = new Point[points.size()];
        int i = 0;
        for (Point point : points) {
            if (point.dt != 0) {
                point.dt = -1;
            }
            pts[i++] = point;
        }
        return pts;
    }

    void removeClient(Client client) {
        emptyClient.addPoints(client.getPoints().toArray(new Point[0]));
        clients.remove(client.getId());
        sendPacket(new ServerPacketRemoveClient(client));
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
                client.addPacket(serverPacket);
            }
        }
    }

    /**
     * Send packet to a client
     *
     * @param serverPacket the packet to send
     * @param client the client to send to
     */
    void sendPacket(ServerPacket serverPacket, Client client) {
        client.addPacket(serverPacket);
    }

    /**
     * Send given packet to all clients of this room
     *
     * @param serverPacket the packet to send
     */
    void sendPacket(ServerPacket serverPacket) {
        for (Client client : clients.values()) {
            client.addPacket(serverPacket);
        }
    }

    @Override
    public String toString() {
        return "Room{id=" + id + "}";
    }
}
