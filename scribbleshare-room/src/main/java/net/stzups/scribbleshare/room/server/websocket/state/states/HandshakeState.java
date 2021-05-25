package net.stzups.scribbleshare.room.server.websocket.state.states;

import io.netty.channel.ChannelHandlerContext;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.InviteCode;
import net.stzups.scribbleshare.data.objects.authentication.AuthenticatedUserSession;
import net.stzups.scribbleshare.data.objects.exceptions.DeserializationException;
import net.stzups.scribbleshare.room.server.RoomHttpServerInitializer;
import net.stzups.scribbleshare.room.server.websocket.Client;
import net.stzups.scribbleshare.room.server.websocket.ClientMessageException;
import net.stzups.scribbleshare.room.server.websocket.Room;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.messages.ClientMessageHandshake;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageAddUser;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageHandshake;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageUpdateDocument;
import net.stzups.scribbleshare.room.server.websocket.state.State;

import java.util.logging.Level;

public class HandshakeState extends State {
    private final AuthenticatedUserSession session;

    public HandshakeState(AuthenticatedUserSession session) {
        this.session = session;
    }

    @Override
    public void message(ChannelHandlerContext ctx, ClientMessage clientMessage) throws ClientMessageException {
        Room room = null;//todo
        switch (clientMessage.getMessageType()) {
            case HANDSHAKE: {
                ClientMessageHandshake clientPacketHandshake = (ClientMessageHandshake) clientMessage;

                Scribbleshare.getLogger(ctx).info("Handshake with invite " + clientPacketHandshake.getCode() + ", " + session);

                Client client = new Client(session.getUser(), ctx.channel());

                client.queueMessage(new ServerMessageHandshake(client));
                InviteCode inviteCode = RoomHttpServerInitializer.getDatabase(ctx).getInviteCode(clientPacketHandshake.getCode());
                client.queueMessage(new ServerMessageAddUser(client.getUser()));
                //figure out which document to open first
                if (inviteCode != null) {
                    Document document = RoomHttpServerInitializer.getDatabase(ctx).getDocument(inviteCode.getDocument());
                    if (document != null) {
                        //if this isn't the user's own document and this isn't part of the user's shared documents then add and update
                        if (document.getOwner() != client.getUser().getId()) {
                            if (client.getUser().getSharedDocuments().add(document.getId())) {
                                try {
                                    RoomHttpServerInitializer.getDatabase(ctx).updateUser(client.getUser());
                                } catch (DatabaseException e) {
                                    e.printStackTrace();
                                    //todo
                                }
                            }
                        }
                        try {
                            room = Room.getRoom(RoomHttpServerInitializer.getDatabase(ctx), document);
                        } catch (DeserializationException e) {
                            throw new ClientMessageException(clientMessage, e);
                        }
                    } else {
                        throw new ClientMessageException(clientMessage, "Somehow used invite code for non existent document");
                    }
                } else {
                    if (client.getUser().getOwnedDocuments().size() == 0) {
                        try {
                            RoomHttpServerInitializer.getDatabase(ctx).createDocument(client.getUser());
                        } catch (DatabaseException e) {
                            Scribbleshare.getLogger().log(Level.WARNING, "Failed to create document for new user with no documents", e);
                        }
                    }
                }
                client.getUser().getOwnedDocuments().removeIf((id) -> {
                    Document document = RoomHttpServerInitializer.getDatabase(ctx).getDocument(id);
                    if (document == null) {
                        return true;
                    } else {
                        client.queueMessage(new ServerMessageUpdateDocument(RoomHttpServerInitializer.getDatabase(ctx).getDocument(id)));
                        return false;
                    }
                });//todo this is bad
                client.getUser().getSharedDocuments().removeIf((id) -> {
                    Document document = RoomHttpServerInitializer.getDatabase(ctx).getDocument(id);
                    if (document == null) {
                        return true;
                    } else {
                        client.queueMessage(new ServerMessageUpdateDocument(RoomHttpServerInitializer.getDatabase(ctx).getDocument(id)));
                        return false;
                    }
                });
                client.flushMessages();

                setState(ctx, new ReadyState(client));
                break;
            }
            default:
                super.message(ctx, clientMessage);
        }
    }
}
