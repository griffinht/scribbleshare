package net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.canvasUpdates;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.Canvas;
import net.stzups.scribbleshare.data.objects.canvas.CanvasObjectWrapper;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.CanvasObject;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdate;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdateType;

import java.util.HashMap;
import java.util.Map;

public class CanvasUpdateMove extends CanvasUpdate {
    private static class CanvasMove {
        private final byte dt;
        private final CanvasObject canvasObject;

        public CanvasMove(ByteBuf byteBuf) {
            this.dt = byteBuf.readByte();
            this.canvasObject = new CanvasObject(byteBuf);
        }

        public CanvasObject getCanvasObject() {
            return canvasObject;
        }

        public void serialize(ByteBuf byteBuf) {
            byteBuf.writeByte(dt);
            canvasObject.serialize(byteBuf);
        }
    }

    private final Map<Short, CanvasMove[]> canvasMovesMap = new HashMap<>();

    public CanvasUpdateMove(ByteBuf byteBuf) {
        super(CanvasUpdateType.MOVE);
        int length = byteBuf.readUnsignedByte();
        for (int i = 0; i < length; i++) {
            short id = byteBuf.readShort();
            CanvasMove[] canvasMoves = new CanvasMove[byteBuf.readUnsignedByte()];
            for (int j = 0; j < canvasMoves.length; j++) {
                canvasMoves[j] = new CanvasMove(byteBuf);
            }
            canvasMovesMap.put(id, canvasMoves);
        }
    }

    @Override
    public void update(Canvas canvas) {
        for (Map.Entry<Short, CanvasMove[]> entry : canvasMovesMap.entrySet()) {
            CanvasObjectWrapper canvasObjectWrapper = canvas.getCanvasObjects().get(entry.getKey());
            if (canvasObjectWrapper == null || entry.getValue().length == 0) {
                System.out.println("oopsie " + canvasObjectWrapper);
                continue;
            }

            canvasObjectWrapper.getCanvasObject().update(entry.getValue()[entry.getValue().length - 1].getCanvasObject());
        }
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeByte((byte) canvasMovesMap.size());
        for (Map.Entry<Short, CanvasMove[]> entry : canvasMovesMap.entrySet()) {
            byteBuf.writeShort(entry.getKey());
            byteBuf.writeByte((byte) entry.getValue().length);
            for (CanvasMove canvasMove : entry.getValue()) {
                canvasMove.serialize(byteBuf);
            }
        }
    }
}
