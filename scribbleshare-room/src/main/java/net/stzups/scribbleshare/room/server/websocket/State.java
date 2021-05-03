package net.stzups.scribbleshare.room.server.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.Attribute;
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

public enum State {
    INITIAL {
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object event) {
            if (event instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
                ServerInitializer.getLogger(ctx).info("WebSocket connection initialized");
                State.HANDSHAKE.setState(ctx);
                return;
            }

            if (event instanceof WebSocketServerProtocolHandler.ServerHandshakeStateEvent) return; //deprecated but still fired

            super.userEventTriggered(ctx, event);
        }
    },
    HANDSHAKE {
        @Override
        public void message(ChannelHandlerContext ctx, ClientMessage clientMessage) throws ClientMessageException {
            switch (clientMessage.getMessageType()) {
                case HANDSHAKE: {
                    ClientMessageHandshake clientPacketHandshake = (ClientMessageHandshake) clientMessage;

                    User user = ScribbleshareRoom.getDatabase().getUser(ctx.channel().attr(HttpAuthenticator.USER).get());
                    if (user == null) {
                        throw new ClientMessageException(clientMessage, "User does not exist");
                    }
                    ServerInitializer.getLogger(ctx).info("Handshake with invite " + clientPacketHandshake.getCode() + ", " + user);

                    Client client = new Client(user, ctx.channel());
                    ClientMessageHandler.getClient(ctx).set(client);

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
                                ClientMessageHandler.getRoom(ctx).set(Room.getRoom(document));
                            } catch (DeserializationException e) {
                                throw new ClientMessageException(clientMessage, e);
                            }
                        } else {
                            throw new ClientMessageException(clientMessage, "Somehow used invite code for non existent document");
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

                    State.READY.setState(ctx);
                    break;
                }
                default:
                    super.message(ctx, clientMessage);
            }
        }
    },
    READY {
        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            final Attribute<Room> room = ClientMessageHandler.getRoom(ctx);
            if (room.get() != null) {
                room.get().removeClient(ClientMessageHandler.getClient(ctx).get());
                return;
            }
            //super.channelInactive(ctx);
        }

        @Override
        public void message(ChannelHandlerContext ctx, ClientMessage clientMessage) throws ClientMessageException {
            final Client client = ClientMessageHandler.getClient(ctx).get();
            final Attribute<Room> room = ClientMessageHandler.getRoom(ctx);

            switch (clientMessage.getMessageType()) {
                case CANVAS_UPDATE: {
                    CanvasUpdates[] canvasUpdates = ((ClientMessageCanvasUpdate) clientMessage).getCanvasUpdatesArray();
                    try {
                        room.get().getCanvas().update(canvasUpdates);
                    } catch (CanvasUpdateException e) {
                        throw new ClientMessageException(clientMessage, e);
                    }
                    room.get().queueMessageExcept(new ServerMessageCanvasUpdate(canvasUpdates), client);
                    break;
                }
                case OPEN_DOCUMENT: {
                    ClientMessageOpenDocument clientPacketOpenDocument = (ClientMessageOpenDocument) clientMessage;
                    Document document = ScribbleshareRoom.getDatabase().getDocument(clientPacketOpenDocument.getId());
                    if (document != null) {
                        if (room.get() != null) {
                            room.get().removeClient(client);
                        }
                        try {
                            room.set(Room.getRoom(document));
                        } catch (DeserializationException e) {
                            throw new ClientMessageException(clientMessage, e);
                        }
                        room.get().addClient(client);
                    } else {
                        ServerInitializer.getLogger(ctx).warning(client + " tried to open document not that does not exist");
                    }
                    break;
                }
                case CREATE_DOCUMENT: {
                    if (room.get() != null) {
                        room.get().removeClient(client);
                    }
                    try {
                        room.set(Room.getRoom(ScribbleshareRoom.getDatabase().createDocument(client.getUser())));
                        ClientMessageHandler.getRoom(ctx).set(room.get());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    client.sendMessage(new ServerMessageUpdateDocument(room.get().getDocument()));
                    room.get().addClient(client);
                    break;
                }
                case DELETE_DOCUMENT: {
                    ClientMessageDeleteDocument clientMessageDeleteDocument = (ClientMessageDeleteDocument) clientMessage;
                    if (clientMessageDeleteDocument.getId() == room.get().getDocument().getId()) {
                        ServerInitializer.getLogger(ctx).info("Deleting live document " + room.get().getDocument());
                        room.get().sendMessage(new ServerMessageDeleteDocument(room.get().getDocument()));
                        room.get().end();
                        ScribbleshareRoom.getDatabase().deleteDocument(room.get().getDocument());
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
                    room.get().getDocument().setName(clientMessageUpdateDocument.getName());
                    room.get().queueMessageExcept(new ServerMessageUpdateDocument(room.get().getDocument()), client);
                    ScribbleshareRoom.getDatabase().updateDocument(room.get().getDocument());
                    break;//todo better update logic
                }
                case GET_INVITE: {
                    client.sendMessage(new ServerMessageGetInvite(ScribbleshareRoom.getDatabase().getInviteCode(room.get().getDocument())));
                    break;
                }
                default:
                    super.message(ctx, clientMessage);
            }
        }
    };

    void setState(ChannelHandlerContext ctx) {
        ServerInitializer.getLogger(ctx).info(this.toString());
        ClientMessageHandler.getState(ctx).set(this);
    }

    public void channelInactive(ChannelHandlerContext ctx) {
        throw new UnsupportedOperationException("Unhandled channel close");
    }

    public void userEventTriggered(ChannelHandlerContext ctx, Object event) {
        throw new UnsupportedOperationException("Unhandled Netty userEventTriggered " + event);
    }

    public void message(ChannelHandlerContext ctx, ClientMessage clientMessage) throws ClientMessageException {
        throw new UnsupportedOperationException("Unhandled ClientMessage " + clientMessage.getClass().getSimpleName());
    }
}
