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
import net.stzups.scribbleshare.server.http.exception.exceptions.InternalServerException;

import java.util.logging.Level;

public class HandshakeState extends State {
    private final AuthenticatedUserSession session;

    public HandshakeState(AuthenticatedUserSession session) {
        this.session = session;
    }

    @Override
    public void message(ChannelHandlerContext ctx, ClientMessage clientMessage) throws ClientMessageException, InternalServerException {
        Room room = null;//todo
        switch (clientMessage.getMessageType()) {
            case HANDSHAKE: {
                ClientMessageHandshake clientPacketHandshake = (ClientMessageHandshake) clientMessage;

                Scribbleshare.getLogger(ctx).info("Handshake with invite " + clientPacketHandshake.getCode() + ", " + session);

                Client client = new Client(session.getUser(), ctx.channel());

                client.queueMessage(new ServerMessageHandshake(client));
                InviteCode inviteCode;
                try {
                    inviteCode = RoomHttpServerInitializer.getDatabase(ctx).getInviteCode(clientPacketHandshake.getCode());
                } catch (DatabaseException e) {
                    throw new InternalServerException(e);
                }
                client.queueMessage(new ServerMessageAddUser(client.getUser()));
                //figure out which document to open first
                if (inviteCode != null) {
                    Document document;
                    try {
                        document = RoomHttpServerInitializer.getDatabase(ctx).getDocument(inviteCode.getDocument());
                    } catch (DatabaseException e) {
                        throw new InternalServerException(e);
                    }
                    if (document == null) {
                        throw new ClientMessageException(clientMessage, "Somehow used invite code for non existent document");
                    }

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
                    if (client.getUser().getOwnedDocuments().size() == 0) {
                        try {
                            RoomHttpServerInitializer.getDatabase(ctx).createDocument(client.getUser());
                        } catch (DatabaseException e) {
                            Scribbleshare.getLogger().log(Level.WARNING, "Failed to create document for new user with no documents", e);
                        }
                    }
                }
                client.getUser().getOwnedDocuments().removeIf((id) -> {
                    Document document;
                    try {
                        document = RoomHttpServerInitializer.getDatabase(ctx).getDocument(id);
                    } catch (DatabaseException e) {
                        throw new RuntimeException(new InternalServerException(e));//todo
                    }
                    if (document == null) {
                        return true;
                    } else {
                        client.queueMessage(new ServerMessageUpdateDocument(document));
                        return false;
                    }
                });//todo this is bad
                client.getUser().getSharedDocuments().removeIf((id) -> {
                    Document document;
                    try {
                        document = RoomHttpServerInitializer.getDatabase(ctx).getDocument(id);
                    } catch (DatabaseException e) {
                        throw new RuntimeException(new InternalServerException(e));//todo
                    }
                    if (document == null) {
                        return true;
                    } else {
                        client.queueMessage(new ServerMessageUpdateDocument(document));
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
