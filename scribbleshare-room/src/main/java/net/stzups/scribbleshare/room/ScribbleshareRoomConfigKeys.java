package net.stzups.scribbleshare.room;

import net.stzups.scribbleshare.util.config.ConfigKey;
import net.stzups.scribbleshare.util.config.OptionalConfigKey;
import net.stzups.scribbleshare.util.config.RequiredConfigKey;

public class ScribbleshareRoomConfigKeys {
    public static final ConfigKey<Integer> PORT = new OptionalConfigKey<>("port", 8080);
    public static final ConfigKey<Boolean> SSL = new RequiredConfigKey<>("ssl");
    public static final ConfigKey<String> SSL_KEYSTORE_PATH = new RequiredConfigKey<>("ssl.keystore.path");
    public static final ConfigKey<String> SSL_KEYSTORE_PASSPHRASE = new RequiredConfigKey<>("ssl.keystore.passphrase");
}
