package net.stzups.scribbleshare;

import net.stzups.config.Config;
import net.stzups.config.ConfigKey;
import net.stzups.config.OptionalConfigKey;
import net.stzups.config.RequiredConfigKey;

public class ScribbleshareConfigImplementation extends Config implements ScribbleshareConfig {
    private static final ConfigKey<String> NAME = new OptionalConfigKey<>("name", "Scribbleshare");
    private static final ConfigKey<Integer> PORT = new OptionalConfigKey<>("port", 80);
    private static final ConfigKey<String> SSL_PATH = new RequiredConfigKey<>("ssl.path");
    private static final ConfigKey<String> SSL_ROOT_PATH = new RequiredConfigKey<>("ssl.root.path");
    private static final ConfigKey<String> ENVIRONMENT_VARIABLE_PREFIX = new OptionalConfigKey<>("environment.variable.prefix", "scribbleshare.");
    private static final ConfigKey<String> PROPERTIES = new OptionalConfigKey<>("properties", "scribbleshare.properties");

    private static final ConfigKey<String> POSTGRES_URL = new RequiredConfigKey<>("postgres.url");
    private static final ConfigKey<String> POSTGRES_USER = new RequiredConfigKey<>("postgres.user");
    private static final ConfigKey<String> POSTGRES_PASSWORD = new RequiredConfigKey<>("postgres.password");
    private static final ConfigKey<Integer> POSTGRES_RETRIES = new OptionalConfigKey<>("postgres.retries", 3);
    private static final ConfigKey<String> DOMAIN = new OptionalConfigKey<>("domain", "localhost");

    private static final ConfigKey<Boolean> SSL = new RequiredConfigKey<>("ssl");

    //public static final ConfigKey<String> REDIS_URL = new RequiredConfigKey<>("redis.url");

    public static final ConfigKey<Boolean> DEBUG_LOG_TRAFFIC = new OptionalConfigKey<>("debug.log.traffic", false);

    @Override
    public String getEnvironmentVariablePrefix() {
        return getString(ENVIRONMENT_VARIABLE_PREFIX);
    }

    @Override
    public String getProperties() {
        return getString(PROPERTIES);
    }

    @Override
    public int getPort() {
        return getInteger(PORT);
    }

    @Override
    public String getDomain() {
        return getString(DOMAIN);
    }

    @Override
    public String getName() {
        return getString(NAME);
    }

    @Override
    public String getUrl() {
        return getString(POSTGRES_URL);
    }

    @Override
    public String getUser() {
        return getString(POSTGRES_USER);
    }

    @Override
    public String getPassword() {
        return getString(POSTGRES_PASSWORD);
    }

    @Override
    public int getMaxRetries() {
        return getInteger(POSTGRES_RETRIES);
    }

    @Override
    public boolean getSSL() {
        return getBoolean(SSL);
    }

    @Override
    public String getSSLRootPath() {
        return getString(SSL_ROOT_PATH);
    }

    @Override
    public String getSSLPath() {
        return getString(SSL_PATH);
    }

    @Override
    public boolean getDebugLogTraffic() {
        return getBoolean(DEBUG_LOG_TRAFFIC);
    }
}
