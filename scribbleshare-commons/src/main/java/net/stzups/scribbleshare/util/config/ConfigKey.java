package net.stzups.scribbleshare.util.config;

/**
 * A {@link String} key that returns a value of type T
 */
public abstract class ConfigKey<T> {
    private final String key;

    protected ConfigKey(String key) {
        this.key = key;
    }

    final String getKey() {
        return key;
    }

    abstract T getDefaultValue(Exception e);
}
