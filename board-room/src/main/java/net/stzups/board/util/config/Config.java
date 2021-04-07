package net.stzups.board.util.config;

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
    private String find(ConfigKey<?> key) {
        for (ConfigProvider configProvider : configProviders) {
            String value = configProvider.get(key.getKey());
            if (value != null) {
                return value;
            }
        }

        return null;
    }

    public String getString(ConfigKey<String> key) {
        String value = find(key);
        if (value != null) {
            return value;
        }
        return key.getDefaultValue(null);
    }

    public Integer getInteger(ConfigKey<Integer> key) {
        String value = find(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return key.getDefaultValue(e);
            }
        }
        return key.getDefaultValue(null);
    }

    public Boolean getBoolean(ConfigKey<Boolean> key) {
        String value = find(key);
        if (value != null) {
            if (value.equalsIgnoreCase("true")) {
                return true;
            } else if (value.equalsIgnoreCase("false")) {
                return false;
            } else {
                return key.getDefaultValue(new IllegalArgumentException(value + " is not true or false"));
            }
        }
        return key.getDefaultValue(null);
    }
}
