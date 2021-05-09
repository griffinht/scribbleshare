package net.stzups.scribbleshare.config;

/**
 * Provides values for keys to a {@link Config}
 */
public interface ConfigProvider {
    /**
     * Returns a value if this {@link ConfigProvider} has the key, otherwise null
     */
    String get(String key);
}
