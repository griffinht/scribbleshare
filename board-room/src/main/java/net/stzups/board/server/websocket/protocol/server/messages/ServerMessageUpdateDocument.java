package net.stzups.board.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.Document;
import net.stzups.board.data.objects.User;
import net.stzups.board.data.objects.canvas.Canvas;
import net.stzups.board.data.objects.canvas.object.CanvasObject;
import net.stzups.board.data.objects.canvas.object.CanvasObjectType;
import net.stzups.board.server.websocket.Client;
import net.stzups.board.server.websocket.protocol.server.ServerMessage;
import net.stzups.board.server.websocket.protocol.server.ServerMessageType;

import java.util.HashMap;
import java.util.Map;

public class ServerMessageUpdateDocument extends ServerMessage {
    private Map<Client, Canvas> canvasMap;

    public ServerMessageUpdateDocument(Map<Client, Canvas> canvasMap) {
        super(ServerMessageType.UPDATE_DOCUMENT);
        this.canvasMap = canvasMap;
    }

    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeShort((short) canvasMap.size());
        for (Map.Entry<Client, Canvas> entry : canvasMap.entrySet()) {
            byteBuf.writeShort(entry.getKey().getId());
            entry.getValue().serialize(byteBuf);
        }
    }
}
