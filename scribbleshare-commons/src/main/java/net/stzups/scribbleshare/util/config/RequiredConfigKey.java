package net.stzups.scribbleshare.util.config;

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

    @Override
    public void setDefaultValue(T defaultValue) {
        throw new UnsupportedOperationException("Can't set default value of a required key");
    }
}
