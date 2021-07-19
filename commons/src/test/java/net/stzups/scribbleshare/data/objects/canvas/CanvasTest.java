package net.stzups.scribbleshare.data.objects.canvas;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.stzups.scribbleshare.data.objects.exceptions.DeserializationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CanvasTest {
    @Test
    public void serializeEmpty() throws DeserializationException {
        Canvas canvas = new Canvas();

        ByteBuf byteBuf = Unpooled.buffer();
        canvas.serialize(byteBuf);

        Canvas c;
        c = new Canvas(byteBuf);
        byteBuf.release();

        assertEquals(canvas, c);
    }

    private static final int MULTIPLE_AMOUNT = 5;

    @Test
    public void serializeEmptyMultiple() throws DeserializationException {
        Canvas[] canvases = new Canvas[MULTIPLE_AMOUNT];
        for (int i = 0; i < canvases.length; i++) {
            canvases[i] = new Canvas();
        }

        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeInt(canvases.length);
        for (Canvas canvas : canvases) {
            canvas.serialize(byteBuf);
        }

        Canvas[] c = new Canvas[byteBuf.readInt()];
        for (int i = 0; i < c.length; i++) {
            c[i] = new Canvas(byteBuf);
        }

        assertArrayEquals(canvases, c);
    }
}
