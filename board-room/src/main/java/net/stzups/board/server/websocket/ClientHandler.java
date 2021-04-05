package net.stzups.board.server.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.stzups.board.BoardRoom;
import net.stzups.board.data.objects.Document;
import net.stzups.board.data.objects.InviteCode;
import net.stzups.board.server.ServerInitializer;
import net.stzups.board.server.websocket.protocol.client.ClientMessage;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageDeleteDocument;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageOpenDocument;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageUpdateCanvas;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageUpdateDocument;
import net.stzups.board.server.websocket.protocol.server.messages.ServerMessageAddUser;
import net.stzups.board.server.websocket.protocol.server.messages.ServerMessageDeleteDocument;
import net.stzups.board.server.websocket.protocol.server.messages.ServerMessageGetInvite;
import net.stzups.board.server.websocket.protocol.server.messages.ServerMessageUpdateDocument;

import java.util.logging.Logger;

public class ClientHandler extends SimpleChannelInboundHandler<ClientMessage> {
    private Logger logger;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        logger = ctx.channel().attr(ServerInitializer.LOGGER).get();
    }

    private final Client client;
    private Room room;

    ClientHandler(Client client, InviteCode inviteCode) {
        this.client = client;
        client.queueMessage(new ServerMessageAddUser(client.getUser()));
        for (long id : client.getUser().getOwnedDocuments()) {
            client.queueMessage(new ServerMessageUpdateDocument(BoardRoom.getDatabase().getDocument(id)));//todo aggregate
        }
        //figure out which document to open first
        if (inviteCode != null) {//todo shared documents
            Document document = BoardRoom.getDatabase().getDocument(inviteCode.getDocument());
            if (document != null) {
                room = Room.getRoom(document);
                client.flushMessages();
                return;
            } else {
                logger.warning(client + " somehow used invite code for non existent document");
            }
        } else {
            if (client.getUser().getOwnedDocuments().length == 0) {
                client.queueMessage(new ServerMessageUpdateDocument(BoardRoom.getDatabase().createDocument(client.getUser())));//todo
            }
        }
        client.flushMessages();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (room != null) {
            room.removeClient(client);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ClientMessage message) {
        switch (message.getMessageType()) {
            case UPDATE_CANVAS: {
                room.updateClient(client, ((ClientMessageUpdateCanvas) message).getCanvasObjects());
                break;
            }
            case OPEN_DOCUMENT: {
                ClientMessageOpenDocument clientPacketOpenDocument = (ClientMessageOpenDocument) message;
                Document document = BoardRoom.getDatabase().getDocument(clientPacketOpenDocument.getId());
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
                    room = Room.getRoom(BoardRoom.getDatabase().createDocument(client.getUser()));
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
                    BoardRoom.getDatabase().deleteDocument(room.getDocument());
                    break;
                }
                Document document = BoardRoom.getDatabase().getDocument(clientMessageDeleteDocument.id());
                if (document == null) {
                    logger.warning(client + " tried to delete document that does not exist");
                    break;
                }
                if (!document.getOwner().equals(client.getUser())) {
                    logger.warning(client + " tried to delete document they do not own");
                    break;
                }
                System.out.println("document is delete");
                BoardRoom.getDatabase().deleteDocument(room.getDocument());
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
                BoardRoom.getDatabase().updateDocument(room.getDocument());
                break;//todo better update logic
            }
            case GET_INVITE: {
                client.sendMessage(new ServerMessageGetInvite(BoardRoom.getDatabase().getInviteCode(room.getDocument())));
                break;
            }
            default:
                throw new UnsupportedOperationException("Unsupported message type " + message.getMessageType() + " sent by " + client);
        }
    }
}
