package net.stzups.board.config;

import java.util.List;

/**
 * Used to store and retrieve key-value pairs by finding the first result from many different strategies.
 */
public class ConfigProvider {//todo probably needs a better name
    private List<StringConfig> stringConfigs;

    /**
     * Constructs a new ConfigProvider from its builder
     * @param stringConfigs configs for String
     */
    ConfigProvider(List<StringConfig> stringConfigs) {
        this.stringConfigs = stringConfigs;
    }

    /**
     * Searches all StringConfigs for a key and gets
     * @param key the key to match
     * @return the matching string, or null if none of the string configs have the key
     */
    public String get(String key) {
        for (Config<String> config : stringConfigs) {
            String value = config.get(key);
            if (value != null) {
                return value;
            }
        }

        return null;
    }

    /**
     * Searches all configs that provide a String value for a key, which if not found will instead return the defaultValue
     * @param key the key to match
     * @param defaultValue the value to return if a value for the key is not found
     * @return the value for the key, or the defaultValue
     */
    public String get(String key, String defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
