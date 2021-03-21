package net.stzups.board.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.Document;
import net.stzups.board.data.objects.User;
import net.stzups.board.server.websocket.protocol.server.ServerMessage;
import net.stzups.board.server.websocket.protocol.server.ServerMessageType;

public class ServerMessageUpdateDocument extends ServerMessage {
    private User user;
    private Document document;

    public ServerMessageUpdateDocument(User user, Document document) {
        super(ServerMessageType.UPDATE_DOCUMENT);
        this.user = user;
        this.document = document;
    }

    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        //document.getCanvas().serialize(user, byteBuf);
    }
}
