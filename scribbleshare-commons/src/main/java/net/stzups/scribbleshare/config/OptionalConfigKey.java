package net.stzups.scribbleshare.config;

/**
 * A {@link ConfigKey} that can use a default value if no other value is found
 */
public class OptionalConfigKey<T> extends ConfigKey<T> {
    private final T defaultValue;

    public OptionalConfigKey(String key, T defaultValue) {
        super(key);
        this.defaultValue = defaultValue;
    }

    T getDefaultValue(Exception e) {
        if (e != null) {
            new Exception("Non fatal exception while parsing value for key \"" + getKey() + "\", default value will be used", e).printStackTrace();
        }
        return defaultValue;
    }
}
