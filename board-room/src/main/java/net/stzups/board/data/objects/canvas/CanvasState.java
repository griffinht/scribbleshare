package net.stzups.board.data.objects.canvas;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.User;
import net.stzups.board.data.objects.canvas.object.wrappers.CanvasObjectsStateWrapper;

import java.util.HashMap;
import java.util.Map;

public class CanvasState {
    private Map<User, Map<Short, CanvasObjectsStateWrapper>> canvasObjects = new HashMap<>();

    CanvasState() {

    }

    /**
     * Updates this CanvasState with a new CanvasState
     */
    public void update(User user, Map<Short, CanvasObjectsStateWrapper> map) {
        canvasObjects.put(user, map);
    }

    public void serialize(User user, ByteBuf byteBuf) {
       byteBuf.writeShort(canvasObjects.size() - (canvasObjects.containsKey(user) ? 1 : 0));
       for (Map.Entry<User, Map<Short, CanvasObjectsStateWrapper>> entry : canvasObjects.entrySet()) {
           if (!entry.getKey().equals(user)) {
               byteBuf.writeShort(entry.getValue().size());
               for (CanvasObjectsStateWrapper canvasObjectsStateWrapper : entry.getValue().values()) {
                   canvasObjectsStateWrapper.serialize(byteBuf);
               }
           }
       }
    }

    public void clear() {
        canvasObjects.clear();
    }
}
