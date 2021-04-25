package net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.canvasUpdates;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.Canvas;
import net.stzups.scribbleshare.data.objects.canvas.CanvasObjectWrapper;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.CanvasObject;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.CanvasObjectType;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdate;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdateType;

import java.util.HashMap;
import java.util.Map;

public class CanvasUpdateInsert extends CanvasUpdate {
    private static class CanvasInsert {
        private final byte dt;
        private final short id;
        private final CanvasObject canvasObject;

        public CanvasInsert(CanvasObjectType type, ByteBuf byteBuf) {
            this.dt = byteBuf.readByte();
            this.id = byteBuf.readShort();
            this.canvasObject = CanvasObject.getCanvasObject(type, byteBuf);
        }

        public void serialize(ByteBuf byteBuf) {
            byteBuf.writeByte(dt);
            byteBuf.writeShort(id);
            canvasObject.serialize(byteBuf);
        }

        public short getId() {
            return id;
        }

        public CanvasObject getCanvasObject() {
            return canvasObject;
        }
    }

    private final Map<CanvasObjectType, CanvasInsert[]> canvasInsertsMap = new HashMap<>();

    public CanvasUpdateInsert(ByteBuf byteBuf) {
        super(CanvasUpdateType.INSERT);
        int length = byteBuf.readUnsignedByte();
        for (int i = 0; i < length; i++) {
            CanvasObjectType type = CanvasObjectType.valueOf(byteBuf.readUnsignedByte());
            CanvasInsert[] canvasInserts = new CanvasInsert[byteBuf.readUnsignedByte()];
            for (int j = 0; j < canvasInserts.length; j++) {
                canvasInserts[j] = new CanvasInsert(type, byteBuf);
            }
            canvasInsertsMap.put(type, canvasInserts);
        }
    }

    @Override
    public void update(Canvas canvas) {
        for (Map.Entry<CanvasObjectType, CanvasInsert[]> entry : canvasInsertsMap.entrySet()) {
            for (CanvasInsert canvasInsert : entry.getValue()) {
                canvas.getCanvasObjects().put(canvasInsert.getId(), new CanvasObjectWrapper(entry.getKey(), canvasInsert.getCanvasObject()));
            }
        }
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeByte((byte) canvasInsertsMap.size());
        for (Map.Entry<CanvasObjectType, CanvasInsert[]> entry : canvasInsertsMap.entrySet()) {
            byteBuf.writeByte((byte) entry.getKey().getId());
            byteBuf.writeByte((byte) entry.getValue().length);
            for (CanvasInsert canvasInsert : entry.getValue()) {
                canvasInsert.serialize(byteBuf);
            }
        }
    }
}
