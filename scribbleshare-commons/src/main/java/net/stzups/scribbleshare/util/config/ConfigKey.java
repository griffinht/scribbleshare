package net.stzups.scribbleshare.util.config;

public abstract class ConfigKey<T> {
    private final String key;

    protected ConfigKey(String key) {
        this.key = key;
    }

    final String getKey() {
        return key;
    }

    abstract T getDefaultValue(Exception e);

    public abstract void setDefaultValue(T defaultValue);
}
