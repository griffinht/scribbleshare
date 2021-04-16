package net.stzups.board.room.server.websocket;

import net.stzups.board.data.objects.Document;
import net.stzups.board.data.objects.canvas.Canvas;
import net.stzups.board.data.objects.canvas.object.CanvasObjectType;
import net.stzups.board.data.objects.canvas.object.CanvasObjectWrapper;
import net.stzups.board.room.BoardRoom;
import net.stzups.board.room.server.websocket.protocol.server.ServerMessage;
import net.stzups.board.room.server.websocket.protocol.server.messages.ServerMessageAddClient;
import net.stzups.board.room.server.websocket.protocol.server.messages.ServerMessageRemoveClient;
import net.stzups.board.room.server.websocket.protocol.server.messages.ServerMessageUpdateCanvas;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

class Room {
    private static final int SEND_PERIOD = 1000;

    private static final  Map<Document, Room> rooms = new HashMap<>();
    static {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (Room room : rooms.values()) {
                    room.update();
                }
            }
        }, 0, SEND_PERIOD);
    }

    private final Set<Client> clients = new HashSet<>();

    private final Canvas canvas;

    Room(Document document) {
        this.canvas = BoardRoom.getDatabase().getCanvas(document);
        rooms.put(document, this);
        BoardRoom.getLogger().info("Started " + this);
    }

    static Room getRoom(Document document) {
        Room room = rooms.get(document);
        if (room == null) {
            return new Room(document);
        }
        return room;
    }

    void end() {
        rooms.remove(canvas.getDocument());
        BoardRoom.getDatabase().saveCanvas(canvas);//todo save interval and dirty flags
        //todo
        BoardRoom.getLogger().info("Ended room " + this);
    }

    Document getDocument() {
        return canvas.getDocument();
    }

    void updateClient(Client client, Map<CanvasObjectType, Map<Short, CanvasObjectWrapper>> canvasObjects) {
        canvas.update(canvasObjects);
        ServerMessageUpdateCanvas serverMessageUpdateCanvas = new ServerMessageUpdateCanvas(canvasObjects);
        queueMessageExcept(serverMessageUpdateCanvas, client);
    }

    /**
     * Creates a new client using its channel.
     * todo
     */
    void addClient(Client client) {

        //for the new client
        //client.sendMessage(new ServerMessageOpenDocument(document));todo remove
        //for the existing clients
        queueMessage(new ServerMessageAddClient(client));
        client.queueMessage(new ServerMessageAddClient(clients));//todo
        clients.add(client);
        flushMessages();
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
        if (clients.isEmpty()) {
            end();
        }
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
        flushMessages();
    }

    @Override
    public String toString() {
        return "Room{canvas=" + canvas + "}";
    }
}
