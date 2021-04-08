package net.stzups.board.room;

import net.stzups.board.util.config.ConfigKey;
import net.stzups.board.util.config.OptionalConfigKey;
import net.stzups.board.util.config.RequiredConfigKey;

public class BoardConfigKeys {
    public static final ConfigKey<String> POSTGRES_URL = new RequiredConfigKey<>("postgres.url");
    public static final ConfigKey<String> POSTGRES_USER = new RequiredConfigKey<>("postgres.user");
    public static final ConfigKey<String> POSTGRES_PASSWORD = new RequiredConfigKey<>("postgres.password");
    public static final ConfigKey<Integer> POSTGRES_RETRIES = new OptionalConfigKey<>("postgres.retries", 3);
    public static final ConfigKey<Boolean> SSL = new RequiredConfigKey<>("ssl");
    public static final ConfigKey<String> SSL_KEYSTORE_PATH = new RequiredConfigKey<>("ssl.keystore.path");
    public static final ConfigKey<String> SSL_KEYSTORE_PASSPHRASE = new RequiredConfigKey<>("ssl.keystore.passphrase");
    public static final ConfigKey<Boolean> DEBUG_LOG_TRAFFIC = new OptionalConfigKey<>("debug.log.traffic", false);
    public static final ConfigKey<String> REDIS_URL = new RequiredConfigKey<>("redis.url");
    public static final ConfigKey<Integer> REDIS_PORT = new OptionalConfigKey<>("redis.port", 6390);
    public static final ConfigKey<String> BOARD_PROPERTIES = new OptionalConfigKey<>("board.properties", "board.properties");
}
