package net.stzups.board.server.websocket;

import net.stzups.board.BoardRoom;
import net.stzups.board.data.objects.Document;
import net.stzups.board.data.objects.canvas.Canvas;
import net.stzups.board.data.objects.canvas.object.CanvasObject;
import net.stzups.board.data.objects.canvas.object.CanvasObjectType;
import net.stzups.board.server.websocket.protocol.server.ServerMessage;
import net.stzups.board.server.websocket.protocol.server.messages.ServerMessageAddClient;
import net.stzups.board.server.websocket.protocol.server.messages.ServerMessageOpenDocument;
import net.stzups.board.server.websocket.protocol.server.messages.ServerMessageRemoveClient;
import net.stzups.board.server.websocket.protocol.server.messages.ServerMessageUpdateCanvas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

class Room {
    private static final int SEND_PERIOD = 1000;

    private static List<Room> rooms = new ArrayList<>();
    static {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (Room room : rooms) {
                    room.update();
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

    void updateClient(Client client, Map<CanvasObjectType, Map<Short, CanvasObject>> canvasObjects) {
        client.getCanvas().update(canvasObjects);
        document.getCanvas().update(canvasObjects);
    }

    /**
     * Creates a new client using its channel.
     * todo
     */
    void addClient(Client client) {

        //for the new client
        //client.sendMessage(new ServerMessageOpenDocument(document));todo remove
        //for the existing clients
        client.sendMessage(new ServerMessageOpenDocument(document));
        sendMessage(new ServerMessageAddClient(client));
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
        sendMessage(new ServerMessageRemoveClient(client));
        BoardRoom.getLogger().info("Removed " + client + " to " + this);
    }

    /**
     * Send given message to all members of the room except for the specified client
     *
     * @param serverMessage message to send
     * @param except client to exclude
     */
    void sendPacketExcept(ServerMessage serverMessage, Client except) {
        for (Client client : clients) {
            if (except != client) {
                client.sendMessage(serverMessage);
            }
        }
    }

    /**
     * Send given message to all clients of this room
     *
     * @param serverMessage the message to send
     */
    void sendMessage(ServerMessage serverMessage) {
        for (Client client : clients) {
            client.sendMessage(serverMessage);
        }
    }

    void queueMessageExcept(ServerMessage serverMessage, Client except) {
        for (Client client : clients) {
            if (except != client) {
                client.queueMessage(serverMessage);
            }
        }
    }

    void queueMessage(ServerMessage serverMessage) {
        for (Client client : clients) {
            client.queueMessage(serverMessage);
        }
    }

    void flushMessages() {
        for (Client client : clients) {
            client.flushMessages();
        }
    }

    private void update() {
        for (Client client : clients) {
            Map<Client, Canvas> canvasMap = new HashMap<>();
            for (Client c : clients) {
                if (c == client) continue;
                canvasMap.put(client, client.getCanvas());
            }
            client.queueMessage(new ServerMessageUpdateCanvas(canvasMap));
        }
        flushMessages();
        for (Client client : clients) {
            client.getCanvas().clear();
        }
    }

    @Override
    public String toString() {
        return "Room{document=" + document + "}";
    }
}
