package net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.canvasUpdates;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.Canvas;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdate;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdateType;

public class CanvasUpdateMouseMove extends CanvasUpdate {
    private static class MouseMove {
        private final byte dt;
        private final short x;
        private final short y;
        private MouseMove(ByteBuf byteBuf) {
            this.dt = byteBuf.readByte();
            this.x = byteBuf.readShort();
            this.y = byteBuf.readShort();
        }

        private void serialize(ByteBuf byteBuf) {
            byteBuf.writeByte(this.dt);
            byteBuf.writeShort(this.x);
            byteBuf.writeShort(this.y);
        }
    }
    private short client;
    private final MouseMove[][] mouseMoves;

    public CanvasUpdateMouseMove(ByteBuf byteBuf) {
        super(CanvasUpdateType.MOUSEMOVE);
        mouseMoves = new MouseMove[byteBuf.readUnsignedByte()][];
        for (int i = 0; i < mouseMoves.length; i++) {
            mouseMoves[i] = new MouseMove[byteBuf.readUnsignedByte()];
            for (int j = 0; j < mouseMoves[i].length; j++) {
                mouseMoves[i][j] = new MouseMove(byteBuf);
            }
        }
    }

    //this is dumb
    //also don't forget to do this
    public void setClient(short client) {
        this.client = client;
    }

    @Override
    public void update(Canvas canvas) {

    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeShort(client);
        byteBuf.writeByte((byte) mouseMoves.length);
        for (MouseMove[] mouseMoves : mouseMoves) {
            byteBuf.writeByte((byte) mouseMoves.length);
            for (MouseMove mouseMove : mouseMoves) {
                mouseMove.serialize(byteBuf);
            }
        }
    }
}
