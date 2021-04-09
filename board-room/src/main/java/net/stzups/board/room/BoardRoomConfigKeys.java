package net.stzups.board.room;

import net.stzups.board.util.config.ConfigKey;
import net.stzups.board.util.config.OptionalConfigKey;
import net.stzups.board.util.config.RequiredConfigKey;

public class BoardRoomConfigKeys {
    public static final ConfigKey<String> BOARD_PROPERTIES = new OptionalConfigKey<>("board.properties", "board.properties");

    public static final ConfigKey<Boolean> SSL = new RequiredConfigKey<>("ssl");
    public static final ConfigKey<String> SSL_KEYSTORE_PATH = new RequiredConfigKey<>("ssl.keystore.path");
    public static final ConfigKey<String> SSL_KEYSTORE_PASSPHRASE = new RequiredConfigKey<>("ssl.keystore.passphrase");

    public static final ConfigKey<Integer> WS_PORT = new OptionalConfigKey<>("http.port", 80);
    public static final ConfigKey<Integer> WSS_PORT = new OptionalConfigKey<>("https.port", 443);

    public static final ConfigKey<Boolean> DEBUG_LOG_TRAFFIC = new OptionalConfigKey<>("debug.log.traffic", false);
}
