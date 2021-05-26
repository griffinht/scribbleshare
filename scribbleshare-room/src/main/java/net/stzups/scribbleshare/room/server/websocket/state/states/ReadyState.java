package net.stzups.scribbleshare.room.server.websocket.state.states;

import io.netty.channel.ChannelHandlerContext;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.exceptions.DeserializationException;
import net.stzups.scribbleshare.room.server.RoomHttpServerInitializer;
import net.stzups.scribbleshare.room.server.websocket.Client;
import net.stzups.scribbleshare.room.server.websocket.ClientMessageException;
import net.stzups.scribbleshare.room.server.websocket.Room;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.messages.ClientMessageOpenDocument;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageUpdateDocument;
import net.stzups.scribbleshare.room.server.websocket.state.State;
import net.stzups.scribbleshare.server.http.exception.exceptions.InternalServerException;

import java.util.logging.Level;

public class ReadyState extends State {
    private final Client client;

    ReadyState(Client client) {
        this.client = client;
    }

    @Override
    public void message(ChannelHandlerContext ctx, ClientMessage clientMessage) throws ClientMessageException {

        switch (clientMessage.getMessageType()) {
            case OPEN_DOCUMENT: {
                ClientMessageOpenDocument clientPacketOpenDocument = (ClientMessageOpenDocument) clientMessage;
                Document document = RoomHttpServerInitializer.getDatabase(ctx).getDocument(clientPacketOpenDocument.getId());
                if (document != null) {
                    //open
                    Room room;
                    try {
                        room = Room.getRoom(RoomHttpServerInitializer.getDatabase(ctx), document);
                    } catch (DeserializationException | InternalServerException e) {
                        throw new ClientMessageException(clientMessage, e);
                    }
                    room.addClient(client);
                    setState(ctx, new RoomState(client, room));
                } else {
                    Scribbleshare.getLogger(ctx).warning(client + " tried to open document not that does not exist");
                }
                break;
            }
            case CREATE_DOCUMENT: {
                //create
                Document document;
                try {
                    document = RoomHttpServerInitializer.getDatabase(ctx).createDocument(client.getUser());
                } catch (DatabaseException e) {
                    Scribbleshare.getLogger().log(Level.WARNING, "Failed to create document for client that requested it", e);//todo this will probably break the client
                    return;//todo throw server exception
                }
                client.sendMessage(new ServerMessageUpdateDocument(document));
                //open
                Room room;
                try {
                    room = Room.getRoom(RoomHttpServerInitializer.getDatabase(ctx), document);
                } catch (DeserializationException | InternalServerException e) {
                    throw new ClientMessageException(clientMessage, e);
                }
                room.addClient(client);
                setState(ctx, new RoomState(client, room));
                break;
            }
/*            case DELETE_DOCUMENT: {
                ClientMessageDeleteDocument clientMessageDeleteDocument = (ClientMessageDeleteDocument) clientMessage;
                if (clientMessageDeleteDocument.getId() == room.getDocument().getId()) {
                    Scribbleshare.getLogger(ctx).info("Deleting live document " + room.getDocument());
                    room.sendMessage(new ServerMessageDeleteDocument(room.getDocument()));
                    room.end();
                    RoomHttpServerInitializer.getDatabase(ctx).deleteDocument(room.getDocument());
                    break;
                } else {
                    throw new ClientMessageException(clientMessage, "Tried to delete document which is not currently open");
                }
*//*                        Document document = ScribbleshareRoom.getDatabase().getDocument(clientMessageDeleteDocument.getId());
                if (document == null) {
                    throw new MessageException(clientMessage, "Tried to delete document with id " + clientMessageDeleteDocument.getId() + " that does not exist");
                }
                if (document.getOwner() != client.getUser().getId()) {
                    throw new MessageException(clientMessage, "Tried to delete document with id " + document.getId() +" which they do not own");
                }
                ServerInitializer.getLogger(ctx).info("Deleting dead document " + room.getDocument());
                ScribbleshareRoom.getDatabase().deleteDocument(room.getDocument());*//*
                //Room.getRoom(document);
                //break;//todo better update logic
            }
            case UPDATE_DOCUMENT: {
                ClientMessageUpdateDocument clientMessageUpdateDocument = (ClientMessageUpdateDocument) clientMessage;
                if (clientMessageUpdateDocument.getName().length() > 64) {
                    throw new ClientMessageException(clientMessage, "Tried to change name to string that is too long (" + clientMessageUpdateDocument.getName().length() + ")");
                }
                room.getDocument().setName(clientMessageUpdateDocument.getName());
                room.queueMessageExcept(new ServerMessageUpdateDocument(room.getDocument()), client);
                RoomHttpServerInitializer.getDatabase(ctx).updateDocument(room.getDocument());
                break;//todo better update logic
            }*/
            default:
                super.message(ctx, clientMessage);
        }
    }
}
