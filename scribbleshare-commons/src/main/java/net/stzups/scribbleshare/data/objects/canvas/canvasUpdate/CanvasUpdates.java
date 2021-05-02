package net.stzups.scribbleshare.data.objects.canvas.canvasUpdate;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.CanvasObjectWrapper;

public class CanvasUpdates {
    private final CanvasObjectWrapper canvasObjectWrapper;
    private final CanvasUpdate[] canvasUpdates;

    public CanvasUpdates(ByteBuf byteBuf) {
        canvasObjectWrapper = new CanvasObjectWrapper(byteBuf);
        canvasUpdates = new CanvasUpdate[byteBuf.readUnsignedByte()];
        for (int i = 0; i < canvasUpdates.length; i++) {
            canvasUpdates[i] = CanvasUpdate.getCanvasUpdate(byteBuf);
        }
    }

    public CanvasObjectWrapper getCanvasObjectWrapper() {
        return canvasObjectWrapper;
    }

    public CanvasUpdate[] getCanvasUpdates() {
        return canvasUpdates;
    }
}
