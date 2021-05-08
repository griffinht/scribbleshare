package net.stzups.scribbleshare.util.config;

public class OptionalConfigKey<T> extends ConfigKey<T> {
    private T defaultValue;

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

    @Override
    public void setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
    }
}
