package net.stzups.scribbleshare.room.server.websocket.protocol.server.message;

import static org.junit.jupiter.api.Assertions.*;

import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdates;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageCanvasUpdate;
import org.junit.jupiter.api.Test;

public class ServerMessageCanvasUpdateTest {

    @Test
    public void test() {
        ServerMessageCanvasUpdate serverMessageCanvasUpdate = new ServerMessageCanvasUpdate(new CanvasUpdates[0]);
    }
}
