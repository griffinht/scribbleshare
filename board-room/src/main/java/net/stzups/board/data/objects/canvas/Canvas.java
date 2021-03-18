package net.stzups.board.data.objects.canvas;

import io.netty.buffer.ByteBuf;
import net.stzups.board.BoardRoom;
import net.stzups.board.data.objects.User;
import net.stzups.board.data.objects.canvas.object.wrappers.CanvasObjectsStateWrapper;
import net.stzups.board.data.objects.canvas.object.wrappers.CanvasObjectsWrapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Canvas {
    private Map<User, List<CanvasObjectsWrapper>> canvasObjects = new LinkedHashMap<>();
    private CanvasState canvasState = new CanvasState();

    public Canvas() {

    }

    /**
     * Deserializes canvas from db or client i guess?
     */
    public Canvas(ByteBuf byteBuf) {
        for (int i = 0; i < byteBuf.readUnsignedShort(); i++) {
            User user = BoardRoom.getDatabase().getUser(byteBuf.readLong());//todo slow and blocking probably
            List<CanvasObjectsWrapper> list = new ArrayList<>();
            canvasObjects.put(user, list);
            for (int j = 0; j < byteBuf.readUnsignedShort(); j++) {
                list.add(new CanvasObjectsWrapper(byteBuf));
            }
        }
    }

    /**
     * Adds a partial canvas from a client to this canvas, will then be updated
     */
    public void update(User user, Map<Short, CanvasObjectsStateWrapper> map) {//canvas will be discarded
        canvasState.update(user, map);
    }

    /**
     * Write everything to buffer
     */
    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeShort((short) canvasObjects.size());
        for (Map.Entry<User, List<CanvasObjectsWrapper>> entry : canvasObjects.entrySet()) {
            serialize(entry, byteBuf);
        }
    }

    /**
     * Write only updated objects, should only be called once per update and shared
     */
    public void serialize(User user, ByteBuf byteBuf) {
        canvasState.serialize(user, byteBuf);
    }

    /**
     * Flushes the updates or something
     */
    public void flush() {
        canvasState.clear();
    }

    public void serialize(Map.Entry<User, List<CanvasObjectsWrapper>> entry, ByteBuf byteBuf) {
        byteBuf.writeLong(entry.getKey().getId());
        byteBuf.writeShort((short) entry.getValue().size());
        for (CanvasObjectsWrapper canvasObjectWrapper : entry.getValue()) {
            canvasObjectWrapper.serialize(byteBuf);
        }
    }
}
