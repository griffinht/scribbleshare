package net.stzups.scribbleshare.room.server.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.InviteCode;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.room.ScribbleshareRoom;
import net.stzups.scribbleshare.room.server.HttpAuthenticator;
import net.stzups.scribbleshare.room.server.ServerInitializer;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.messages.ClientMessageDeleteDocument;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.messages.ClientMessageHandshake;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.messages.ClientMessageOpenDocument;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.messages.ClientMessageUpdateCanvas;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.messages.ClientMessageUpdateDocument;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageAddUser;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageDeleteDocument;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageGetInvite;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageUpdateDocument;

import java.util.logging.Logger;

public class MessageHandler extends SimpleChannelInboundHandler<ClientMessage> {
    private enum State {
        HANDSHAKE,
        READY,
    }

    private Logger logger;

    private State state = null;

    private Client client;
    private Room room;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        logger = ctx.channel().attr(ServerInitializer.LOGGER).get();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (room != null) {
            room.removeClient(client);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) {
        if (event instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            logger.info("WebSocket connection initialized");
            state = State.HANDSHAKE;
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ClientMessage message) {
        switch (state) {
            case HANDSHAKE: {
                switch (message.getMessageType()) {
                    case HANDSHAKE: {
                        ClientMessageHandshake clientPacketHandshake = (ClientMessageHandshake) message;
                        User user = ScribbleshareRoom.getDatabase().getUser(ctx.channel().attr(HttpAuthenticator.USER).get());
                        logger.info("Handshake with invite " + clientPacketHandshake.getCode() + ", " + user);

                        state = State.READY;
                        client = new Client(user, ctx.channel());
                        InviteCode inviteCode = ScribbleshareRoom.getDatabase().getInviteCode(clientPacketHandshake.getCode());
                        client.queueMessage(new ServerMessageAddUser(client.getUser()));
                        //figure out which document to open first
                        if (inviteCode != null) {
                            Document document = ScribbleshareRoom.getDatabase().getDocument(inviteCode.getDocument());
                            if (document != null) {
                                //if this isn't the user's own document and this isn't part of the user's shared documents then add and update
                                if (!document.getOwner().equals(client.getUser())) {
                                    if (client.getUser().addSharedDocument(document)) ScribbleshareRoom.getDatabase().updateUser(client.getUser());
                                }
                                room = Room.getRoom(document);
                            } else {
                                logger.warning(client + " somehow used invite code for non existent document");
                                return;
                                //NPE will be thrown later
                            }
                        } else {
                            if (client.getUser().getOwnedDocuments().length == 0) {
                                ScribbleshareRoom.getDatabase().createDocument(client.getUser());
                            }
                        }
                        for (long id : client.getUser().getOwnedDocuments()) {
                            client.queueMessage(new ServerMessageUpdateDocument(ScribbleshareRoom.getDatabase().getDocument(id)));//todo aggregate
                        }
                        for (long id : client.getUser().getSharedDocuments()) {
                            client.queueMessage(new ServerMessageUpdateDocument(ScribbleshareRoom.getDatabase().getDocument(id), true));
                        }
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
                    case UPDATE_CANVAS: {
                        room.updateClient(client, ((ClientMessageUpdateCanvas) message).getCanvasObjects());
                        break;
                    }
                    case OPEN_DOCUMENT: {
                        ClientMessageOpenDocument clientPacketOpenDocument = (ClientMessageOpenDocument) message;
                        Document document = ScribbleshareRoom.getDatabase().getDocument(clientPacketOpenDocument.getId());
                        if (document != null) {
                            if (room != null) {
                                room.removeClient(client);
                            }
                            room = Room.getRoom(document);
                            room.addClient(client);
                        } else {
                            logger.warning(client + " tried to open document not that does not exist");
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
                        if (clientMessageDeleteDocument.id() == room.getDocument().getId()) {
                            System.out.println("document is delete ROOM");
                            room.sendMessage(new ServerMessageDeleteDocument(room.getDocument()));
                            room.end();
                            ScribbleshareRoom.getDatabase().deleteDocument(room.getDocument());
                            break;
                        }
                        Document document = ScribbleshareRoom.getDatabase().getDocument(clientMessageDeleteDocument.id());
                        if (document == null) {
                            logger.warning(client + " tried to delete document that does not exist");
                            break;
                        }
                        if (!document.getOwner().equals(client.getUser())) {
                            logger.warning(client + " tried to delete document they do not own");
                            break;
                        }
                        System.out.println("document is delete");
                        ScribbleshareRoom.getDatabase().deleteDocument(room.getDocument());
                        //Room.getRoom(document);
                        break;//todo better update logic
                    }
                    case UPDATE_DOCUMENT: {
                        ClientMessageUpdateDocument clientMessageUpdateDocument = (ClientMessageUpdateDocument) message;
                        if (clientMessageUpdateDocument.getName().length() > 64) {
                            logger.warning(client + " tried to change name to string that is too long (" + clientMessageUpdateDocument.getName().length() + ")");
                            break;
                        }
                        room.getDocument().setName(clientMessageUpdateDocument.getName());
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
