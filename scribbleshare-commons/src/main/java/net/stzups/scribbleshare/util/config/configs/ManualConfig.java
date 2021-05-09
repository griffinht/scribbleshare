package net.stzups.scribbleshare.util.config.configs;

import net.stzups.scribbleshare.util.config.ConfigProvider;

import java.util.Collections;
import java.util.Map;

public class ManualConfig implements ConfigProvider {
    private final Map<String, String> keyValues;

    public ManualConfig(String key, String value) {
        keyValues = Collections.singletonMap(key, value);
    }

    @Override
    public String get(String key) {
        return keyValues.get(key);
    }
}
