package net.stzups.board.data.objects.canvas.object.wrappers;

import io.netty.buffer.ByteBuf;

public class CanvasObjectsStateWrapper extends CanvasObjectsWrapper {
    private long time;// creation time
    private byte[] dts;// delta times, each correspond to an element

    public CanvasObjectsStateWrapper(ByteBuf byteBuf) {
        super(byteBuf);
        time = byteBuf.readLong();
        dts = new byte[getCanvasObjects().length];
        for (int i = 0; i < dts.length; i++) {
            dts[i] = byteBuf.readByte();
        }
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeLong(time);
        for (byte dt : dts) {
            byteBuf.writeByte(dt);
        }
    }
}
