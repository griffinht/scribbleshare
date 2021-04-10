package net.stzups.board;

import net.stzups.board.util.config.ConfigKey;
import net.stzups.board.util.config.OptionalConfigKey;
import net.stzups.board.util.config.RequiredConfigKey;

public class BoardConfigKeys {
    public static final ConfigKey<String> POSTGRES_URL = new RequiredConfigKey<>("postgres.url");
    public static final ConfigKey<String> POSTGRES_USER = new RequiredConfigKey<>("postgres.user");
    public static final ConfigKey<String> POSTGRES_PASSWORD = new RequiredConfigKey<>("postgres.password");
    public static final ConfigKey<Integer> POSTGRES_RETRIES = new OptionalConfigKey<>("postgres.retries", 3);

    public static final ConfigKey<String> REDIS_URL = new RequiredConfigKey<>("redis.url");
    public static final ConfigKey<Integer> REDIS_PORT = new OptionalConfigKey<>("redis.port", 6379);
}
