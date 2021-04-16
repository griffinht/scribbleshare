package net.stzups.board;

import net.stzups.board.util.config.ConfigKey;
import net.stzups.board.util.config.OptionalConfigKey;
import net.stzups.board.util.config.RequiredConfigKey;

public class BoardConfigKeys {
    public static final ConfigKey<String> BOARD_PROPERTIES = new OptionalConfigKey<>("board.properties", "board.properties");

    public static final ConfigKey<String> POSTGRES_URL = new RequiredConfigKey<>("postgres.url");
    public static final ConfigKey<String> POSTGRES_USER = new RequiredConfigKey<>("postgres.user");
    public static final ConfigKey<String> POSTGRES_PASSWORD = new RequiredConfigKey<>("postgres.password");
    public static final ConfigKey<Integer> POSTGRES_RETRIES = new OptionalConfigKey<>("postgres.retries", 3);

    public static final ConfigKey<String> REDIS_URL = new RequiredConfigKey<>("redis.url");

    public static final ConfigKey<Boolean> DEBUG_LOG_TRAFFIC = new OptionalConfigKey<>("debug.log.traffic", false);
}
