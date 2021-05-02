package net.stzups.scribbleshare.room;

import net.stzups.scribbleshare.util.config.ConfigKey;
import net.stzups.scribbleshare.util.config.OptionalConfigKey;

public class ScribbleshareRoomConfigKeys {
    public static final ConfigKey<String> WEBSOCKET_PATH = new OptionalConfigKey<>("websocket.path", "/scribblesocket");
}
