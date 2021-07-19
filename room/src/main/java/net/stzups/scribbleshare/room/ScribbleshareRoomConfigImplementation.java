package net.stzups.scribbleshare.room;

import net.stzups.config.ConfigKey;
import net.stzups.config.OptionalConfigKey;
import net.stzups.scribbleshare.ScribbleshareConfigImplementation;

public class ScribbleshareRoomConfigImplementation extends ScribbleshareConfigImplementation implements ScribbleshareRoomConfig {
    private static final ConfigKey<String> WEBSOCKET_PATH = new OptionalConfigKey<>("websocket.path", "/scribblesocket");

    public String getWebsocketPath() {
        return getString(WEBSOCKET_PATH);
    }
}