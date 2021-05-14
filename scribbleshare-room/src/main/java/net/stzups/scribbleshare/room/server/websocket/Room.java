package net.stzups.scribbleshare.room.server.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.data.database.ScribbleshareDatabase;
import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.Resource;
import net.stzups.scribbleshare.data.objects.canvas.Canvas;
import net.stzups.scribbleshare.data.objects.exceptions.DeserializationException;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageAddClient;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageAddUser;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageOpenDocument;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageRemoveClient;

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

    private final ScribbleshareDatabase database;
    private final Document document;
    private final Canvas canvas;

    Room(ScribbleshareDatabase database, Document document) throws DeserializationException {
        this.database = database;
        this.document = document;
        ByteBuf canvas = database.getResource(document.getId(), document.getId()).getData();
        this.canvas = new Canvas(canvas);
        rooms.put(document, this);
        Scribbleshare.getLogger().info("Started " + this);
    }

    static Room getRoom(ScribbleshareDatabase database, Document document) throws DeserializationException {
        Room room = rooms.get(document);
        if (room == null) {
            return new Room(database, document);
        }
        return room;
    }

    void end() {
        rooms.remove(document);
        if (canvas.isDirty()) {
            ByteBuf byteBuf = Unpooled.buffer();
            canvas.serialize(byteBuf);
            database.updateResource(document.getId(), document.getId(), new Resource(byteBuf));//todo autosave?
        }
        //todo
        Scribbleshare.getLogger().info("Ended room " + this);
    }

    Document getDocument() {
        return document;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Creates a new client using its channel.
     * todo
     */
    void addClient(Client client) {

        //for the new client
        client.queueMessage(new ServerMessageOpenDocument(document, canvas));//todo
        for (Client c : clients) {
            client.queueMessage(new ServerMessageAddUser(c.getUser()));
        }
        client.queueMessage(new ServerMessageAddClient(clients));//todo
        //for the existing clients
        queueMessage(new ServerMessageAddUser(client.getUser()));
        queueMessage(new ServerMessageAddClient(client));

        clients.add(client);
        flushMessages();
        Scribbleshare.getLogger().info("Added " + client + " to " + this);
    }

    /**
     * Removes given client from room
     *
     * @param client client to remove
     */
    void removeClient(Client client) {
        clients.remove(client);
        sendMessage(new ServerMessageRemoveClient(client));
        Scribbleshare.getLogger().info("Removed " + client + " to " + this);
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
        return "Room{document=" + document + "}";
    }
}
