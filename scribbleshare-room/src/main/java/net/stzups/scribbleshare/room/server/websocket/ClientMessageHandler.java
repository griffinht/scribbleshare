package net.stzups.scribbleshare.room.server.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import net.stzups.scribbleshare.data.exceptions.CanvasUpdateException;
import net.stzups.scribbleshare.data.exceptions.DeserializationException;
import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.InviteCode;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdates;
import net.stzups.scribbleshare.room.ScribbleshareRoom;
import net.stzups.scribbleshare.room.exceptions.ClientMessageException;
import net.stzups.scribbleshare.room.server.HttpAuthenticator;
import net.stzups.scribbleshare.room.server.ServerInitializer;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.messages.ClientMessageCanvasUpdate;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.messages.ClientMessageDeleteDocument;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.messages.ClientMessageHandshake;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.messages.ClientMessageOpenDocument;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.messages.ClientMessageUpdateDocument;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageAddUser;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageCanvasUpdate;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageDeleteDocument;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageGetInvite;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageHandshake;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageUpdateDocument;

public class ClientMessageHandler extends SimpleChannelInboundHandler<ClientMessage> {
    private enum State {
        HANDSHAKE,
        READY,
    }

    private State state = null;

    private Client client;
    private Room room;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (room != null) {
            room.removeClient(client);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) {
        if (event instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            ServerInitializer.getLogger(ctx).info("WebSocket connection initialized");
            state = State.HANDSHAKE;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ServerInitializer.getLogger(ctx).warning("Handling caused test " + cause.getMessage());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ClientMessage message) throws ClientMessageException {
        switch (state) {
            case HANDSHAKE: {
                switch (message.getMessageType()) {
                    case HANDSHAKE: {
                        ClientMessageHandshake clientPacketHandshake = (ClientMessageHandshake) message;
                        User user = ScribbleshareRoom.getDatabase().getUser(ctx.channel().attr(HttpAuthenticator.USER).get());
                        if (user == null) {
                            throw new ClientMessageException(message, "User does not exist");
                        }
                        ServerInitializer.getLogger(ctx).info("Handshake with invite " + clientPacketHandshake.getCode() + ", " + user);

                        state = State.READY;
                        client = new Client(user, ctx.channel());
                        client.queueMessage(new ServerMessageHandshake(client));
                        InviteCode inviteCode = ScribbleshareRoom.getDatabase().getInviteCode(clientPacketHandshake.getCode());
                        client.queueMessage(new ServerMessageAddUser(client.getUser()));
                        //figure out which document to open first
                        if (inviteCode != null) {
                            Document document = ScribbleshareRoom.getDatabase().getDocument(inviteCode.getDocument());
                            if (document != null) {
                                //if this isn't the user's own document and this isn't part of the user's shared documents then add and update
                                if (document.getOwner() != client.getUser().getId()) {
                                    if (client.getUser().getSharedDocuments().add(document.getId())) {
                                        ScribbleshareRoom.getDatabase().updateUser(client.getUser());
                                    }
                                }
                                try {
                                    room = Room.getRoom(document);
                                } catch (DeserializationException e) {
                                    throw new ClientMessageException(message, e);
                                }
                            } else {
                                throw new ClientMessageException(message, "Somehow used invite code for non existent document");
                            }
                        } else {
                            if (client.getUser().getOwnedDocuments().size() == 0) {
                                ScribbleshareRoom.getDatabase().createDocument(client.getUser());
                            }
                        }
                        client.getUser().getOwnedDocuments().removeIf((id) -> {
                            Document document = ScribbleshareRoom.getDatabase().getDocument(id);
                            if (document == null) {
                                return true;
                            } else {
                                client.queueMessage(new ServerMessageUpdateDocument(ScribbleshareRoom.getDatabase().getDocument(id)));
                                return false;
                            }
                        });//todo this is bad
                        client.getUser().getSharedDocuments().removeIf((id) -> {
                            Document document = ScribbleshareRoom.getDatabase().getDocument(id);
                            if (document == null) {
                                return true;
                            } else {
                                client.queueMessage(new ServerMessageUpdateDocument(ScribbleshareRoom.getDatabase().getDocument(id)));
                                return false;
                            }
                        });
                        client.flushMessages();
                        break;
                    }
                    default:
                        throw new UnsupportedOperationException("Unsupported message type " + message.getMessageType() + " for state " + state);
                }
                break;
            }
            case READY: {
                switch (message.getMessageType()) {
                    case CANVAS_UPDATE: {
                        CanvasUpdates[] canvasUpdates = ((ClientMessageCanvasUpdate) message).getCanvasUpdatesArray();
                        try {
                            room.getCanvas().update(canvasUpdates);
                        } catch (CanvasUpdateException e) {
                            throw new ClientMessageException(message, e);
                        }
                        room.queueMessageExcept(new ServerMessageCanvasUpdate(canvasUpdates), client);
                        break;
                    }
                    case OPEN_DOCUMENT: {
                        ClientMessageOpenDocument clientPacketOpenDocument = (ClientMessageOpenDocument) message;
                        Document document = ScribbleshareRoom.getDatabase().getDocument(clientPacketOpenDocument.getId());
                        if (document != null) {
                            if (room != null) {
                                room.removeClient(client);
                            }
                            try {
                                room = Room.getRoom(document);
                            } catch (DeserializationException e) {
                                throw new ClientMessageException(message, e);
                            }
                            room.addClient(client);
                        } else {
                            ServerInitializer.getLogger(ctx).warning(client + " tried to open document not that does not exist");
                        }
                        break;
                    }
                    case CREATE_DOCUMENT: {
                        if (room != null) {
                            room.removeClient(client);
                        }
                        try {
                            room = Room.getRoom(ScribbleshareRoom.getDatabase().createDocument(client.getUser()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        client.sendMessage(new ServerMessageUpdateDocument(room.getDocument()));
                        room.addClient(client);
                        break;
                    }
                    case DELETE_DOCUMENT: {
                        ClientMessageDeleteDocument clientMessageDeleteDocument = (ClientMessageDeleteDocument) message;
                        if (clientMessageDeleteDocument.getId() == room.getDocument().getId()) {
                            ServerInitializer.getLogger(ctx).info("Deleting live document " + room.getDocument());
                            room.sendMessage(new ServerMessageDeleteDocument(room.getDocument()));
                            room.end();
                            ScribbleshareRoom.getDatabase().deleteDocument(room.getDocument());
                            break;
                        } else {
                            throw new ClientMessageException(message, "Tried to delete document which is not currently open");
                        }
/*                        Document document = ScribbleshareRoom.getDatabase().getDocument(clientMessageDeleteDocument.getId());
                        if (document == null) {
                            throw new MessageException(message, "Tried to delete document with id " + clientMessageDeleteDocument.getId() + " that does not exist");
                        }
                        if (document.getOwner() != client.getUser().getId()) {
                            throw new MessageException(message, "Tried to delete document with id " + document.getId() +" which they do not own");
                        }
                        ServerInitializer.getLogger(ctx).info("Deleting dead document " + room.getDocument());
                        ScribbleshareRoom.getDatabase().deleteDocument(room.getDocument());*/
                        //Room.getRoom(document);
                        //break;//todo better update logic
                    }
                    case UPDATE_DOCUMENT: {
                        ClientMessageUpdateDocument clientMessageUpdateDocument = (ClientMessageUpdateDocument) message;
                        if (clientMessageUpdateDocument.getName().length() > 64) {
                            throw new ClientMessageException(message, "Tried to change name to string that is too long (" + clientMessageUpdateDocument.getName().length() + ")");
                        }
                        room.getDocument().setName(clientMessageUpdateDocument.getName());
                        room.queueMessageExcept(new ServerMessageUpdateDocument(room.getDocument()), client);
                        ScribbleshareRoom.getDatabase().updateDocument(room.getDocument());
                        break;//todo better update logic
                    }
                    case GET_INVITE: {
                        client.sendMessage(new ServerMessageGetInvite(ScribbleshareRoom.getDatabase().getInviteCode(room.getDocument())));
                        break;
                    }
                    default:
                        throw new UnsupportedOperationException("Unsupported message type " + message.getMessageType() + " for state " + state + " sent by " + client);
                }
                break;
            }
        }
    }
}
