package net.stzups.board.config;

import java.util.List;

/**
 * Used to store and retrieve key-value pairs by finding the first result from many different strategies.
 */
public class Config {//todo probably needs a better name
    private List<ConfigProvider> configProviders;

    /**
     * Constructs a new ConfigProvider from its builder
     */
    Config(List<ConfigProvider> configProviders) {
        this.configProviders = configProviders;
    }

    /**
     * Gets a String value for a String key from any config provider
     */
    public String get(String key) {
        for (ConfigProvider configProvider : configProviders) {
            String value = configProvider.get(key);
            if (value != null) {
                return value;
            }
        }

        return null;
    }

    //todo @Nullable annotations?
    public Integer getInt(String key) {
        try {
            return Integer.parseInt(get(key));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public Boolean getBoolean(String key) {
        String value = get(key);
        if (value != null) {
            if (value.equalsIgnoreCase("true")) {
                return true;
            } else if (value.equalsIgnoreCase("false")) {
                return false;
            }
        }
        return null;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        } else {
            return Boolean.parseBoolean(value);
        }
    }

    public String get(String key, String defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
