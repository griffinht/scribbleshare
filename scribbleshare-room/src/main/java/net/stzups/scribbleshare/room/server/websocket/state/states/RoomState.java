package net.stzups.scribbleshare.room.server.websocket.state.states;

import io.netty.channel.ChannelHandlerContext;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdateException;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdates;
import net.stzups.scribbleshare.room.server.RoomHttpServerInitializer;
import net.stzups.scribbleshare.room.server.websocket.Client;
import net.stzups.scribbleshare.room.server.websocket.ClientMessageException;
import net.stzups.scribbleshare.room.server.websocket.Room;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.messages.ClientMessageCanvasUpdate;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.messages.ClientMessageDeleteDocument;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.messages.ClientMessageUpdateDocument;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageCanvasUpdate;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageDeleteDocument;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageUpdateDocument;

import java.util.logging.Level;

public class RoomState extends ReadyState {
    private final Client client;
    private final Room room;

    RoomState(Client client, Room room) {
        super(client);
        this.client = client;
        this.room = room;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        room.removeClient(client);
        setState(ctx, new ReadyState(client));
    }

    @Override
    public void message(ChannelHandlerContext ctx, ClientMessage clientMessage) throws ClientMessageException {

        switch (clientMessage.getMessageType()) {
            case CANVAS_UPDATE: {
                CanvasUpdates[] canvasUpdates = ((ClientMessageCanvasUpdate) clientMessage).getCanvasUpdatesArray();
                try {
                    room.getCanvas().update(canvasUpdates);
                } catch (CanvasUpdateException e) {
                    throw new ClientMessageException(clientMessage, e);
                }
                room.queueMessageExcept(new ServerMessageCanvasUpdate(canvasUpdates), client);
                break;
            }
            case OPEN_DOCUMENT: {
                room.removeClient(client);
            }
            case CREATE_DOCUMENT: {
                room.removeClient(client);
            }
            case DELETE_DOCUMENT: {
                ClientMessageDeleteDocument clientMessageDeleteDocument = (ClientMessageDeleteDocument) clientMessage;
                if (clientMessageDeleteDocument.getId() == room.getDocument().getId()) {
                    Scribbleshare.getLogger(ctx).info("Deleting live document " + room.getDocument());
                    room.sendMessage(new ServerMessageDeleteDocument(room.getDocument()));
                    room.end();
                    try {
                        RoomHttpServerInitializer.getDatabase(ctx).deleteDocument(room.getDocument());
                    } catch (DatabaseException e) {
                        Scribbleshare.getLogger().log(Level.WARNING, "Failed to delete document", e);
                        //todo
                    }
                    break;
                } else {
                    throw new ClientMessageException(clientMessage, "Tried to delete document which is not currently open");
                }
/*                        Document document = ScribbleshareRoom.getDatabase().getDocument(clientMessageDeleteDocument.getId());
                if (document == null) {
                    throw new MessageException(clientMessage, "Tried to delete document with id " + clientMessageDeleteDocument.getId() + " that does not exist");
                }
                if (document.getOwner() != client.getUser().getId()) {
                    throw new MessageException(clientMessage, "Tried to delete document with id " + document.getId() +" which they do not own");
                }
                ServerInitializer.getLogger(ctx).info("Deleting dead document " + room.getDocument());
                ScribbleshareRoom.getDatabase().deleteDocument(room.getDocument());*/
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
                try {
                    RoomHttpServerInitializer.getDatabase(ctx).updateDocument(room.getDocument());
                } catch (DatabaseException e) {
                    Scribbleshare.getLogger(ctx).log(Level.WARNING, "Failed to update document", e);
                    //todo
                }
                break;//todo better update logic
            }
            default:
                super.message(ctx, clientMessage);
        }
    }
}
