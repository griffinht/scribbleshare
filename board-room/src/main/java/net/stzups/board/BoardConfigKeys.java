package net.stzups.board;

import net.stzups.board.util.config.ConfigKey;
import net.stzups.board.util.config.OptionalConfigKey;
import net.stzups.board.util.config.RequiredConfigKey;

public class BoardConfigKeys {
    public static final ConfigKey<Boolean> POSTGRES = new RequiredConfigKey<>("postgres");
    public static final ConfigKey<String> POSTGRES_URL = new RequiredConfigKey<>("postgres.url");
    public static final ConfigKey<String> POSTGRES_USER = new RequiredConfigKey<>("postgres.user");
    public static final ConfigKey<String> POSTGRES_PASSWORD = new RequiredConfigKey<>("postgres.password");
    public static final ConfigKey<Boolean> SSL = new RequiredConfigKey<>("ssl");
    public static final ConfigKey<String> SSL_KEYSTORE_PATH = new RequiredConfigKey<>("ssl.keystore.path");
    public static final ConfigKey<String> SSL_KEYSTORE_PASSPHRASE = new RequiredConfigKey<>("ssl.keystore.passphrase");
    public static final ConfigKey<Boolean> DEBUG_LOG_TRAFFIC = new OptionalConfigKey<>("debug.log.traffic", false);
}