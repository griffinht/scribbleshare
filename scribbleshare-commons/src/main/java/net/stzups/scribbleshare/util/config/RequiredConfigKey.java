package net.stzups.scribbleshare.util.config;

/**
 * A {@link ConfigKey} that must have a value, otherwise an exception is thrown
 */
public class RequiredConfigKey<T> extends ConfigKey<T> {
    public RequiredConfigKey(String key) {
        super(key);
    }

    @Override
    T getDefaultValue(Exception e) {
        if (e != null) {
            throw new IllegalArgumentException("Parsing value for required config key \"" + getKey() + "\" caused exception", e);
        } else {
            throw new IllegalArgumentException("Missing value for required config key \"" + getKey() + "\"");
        }
    }
}
