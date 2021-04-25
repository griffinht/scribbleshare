package net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.canvasUpdates;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.Canvas;
import net.stzups.scribbleshare.data.objects.canvas.CanvasObjectWrapper;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.CanvasObject;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.canvasObjects.Line;
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

    private final short id;
    private final byte first;
    private final CanvasMove[] canvasMoves;

    public CanvasUpdateMove(ByteBuf byteBuf) {
        super(CanvasUpdateType.MOVE);
        id = byteBuf.readShort();
        first = byteBuf.readByte();
        canvasMoves = new CanvasMove[byteBuf.readUnsignedByte()];
        for (int i = 0; i < canvasMoves.length; i++) {
            canvasMoves[i] = new CanvasMove(byteBuf);
        }
    }

    @Override
    public void update(Canvas canvas) {
        CanvasObjectWrapper canvasObjectWrapper = canvas.getCanvasObjects().get(id);
        if (canvasObjectWrapper == null || canvasMoves.length == 0) {
            new RuntimeException("oopsie " + canvasObjectWrapper + ", "  + canvasMoves.length).printStackTrace();
            return;
        }

        if (canvasObjectWrapper.getCanvasObject() instanceof Line) {
            Line line = (Line) canvasObjectWrapper.getCanvasObject();
            line.getPoints()
        } else {
            canvasObjectWrapper.getCanvasObject().update(canvasMoves[canvasMoves.length - 1].canvasObject);
        }
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeShort(id);
        byteBuf.writeByte(first);
        byteBuf.writeByte((byte) canvasMoves.length);
        for (CanvasMove canvasMove : canvasMoves) {
            canvasMove.serialize(byteBuf);
        }
    }
}
