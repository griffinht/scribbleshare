package net.stzups.scribbleshare.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Searches through provided {@link List<ConfigProvider>} to find a value for a key
 */
public class Config {
    private final List<ConfigProvider> configProviders;

    protected Config() {
        configProviders = new ArrayList<>();
    }

    public Config addConfigProvider(ConfigProvider configProvider) {
        configProviders.add(configProvider);
        return this;
    }

    public Config removeConfigProvider(ConfigProvider configProvider) {
        configProviders.remove(configProvider);
        return this;
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

    protected String getString(ConfigKey<String> key) {
        String value = find(key);
        if (value != null) {
            return value;
        }
        return key.getDefaultValue(null);
    }

    protected Integer getInteger(ConfigKey<Integer> key) {
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

    protected Boolean getBoolean(ConfigKey<Boolean> key) {
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
