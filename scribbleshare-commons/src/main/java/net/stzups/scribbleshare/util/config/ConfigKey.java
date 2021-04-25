package net.stzups.scribbleshare.util.config;

public class ConfigKey<T> {
    private final String key;

    ConfigKey(String key) {
        this.key = key;
    }

    String getKey() {
        return key;
    }

    T getDefaultValue(Exception e) {
        throw new UnsupportedOperationException("Method should be overridden");
    }
}
