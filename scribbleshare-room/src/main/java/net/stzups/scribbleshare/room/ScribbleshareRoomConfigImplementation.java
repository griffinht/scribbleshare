package net.stzups.scribbleshare.room;

import net.stzups.scribbleshare.ScribbleshareConfigImplementation;
import net.stzups.scribbleshare.util.config.ConfigKey;
import net.stzups.scribbleshare.util.config.OptionalConfigKey;

public class ScribbleshareRoomConfigImplementation extends ScribbleshareConfigImplementation implements ScribbleshareRoomConfig {
    private static final ConfigKey<String> WEBSOCKET_PATH = new OptionalConfigKey<>("websocket.path", "/scribblesocket");

    public String getWebsocketPath() {
        return getString(WEBSOCKET_PATH);
    }
}
