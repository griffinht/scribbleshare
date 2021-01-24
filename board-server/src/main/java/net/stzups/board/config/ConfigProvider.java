package net.stzups.board.config;

import java.util.ArrayList;
import java.util.List;

public class ConfigProvider {
    private List<StringConfig> stringConfigs = new ArrayList<>();
    private List<CharArrayConfig> charArrayConfigs = new ArrayList<>();

    ConfigProvider(List<Config> configs) {
        for (Config config : configs) {
            if (config instanceof StringConfig) {
                stringConfigs.add((StringConfig) config);
            } else if (config instanceof CharArrayConfig) {
                charArrayConfigs.add((CharArrayConfig) config);
            }
        }
    }

    public String get(String key) {
        for (StringConfig stringConfig : stringConfigs) {
            String value = stringConfig.get(key);
            if (value != null) {
                return value;
            }
        }

        return null;
    }

    public String get(String key, String defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public char[] getCharArray(String key) {
        for (CharArrayConfig charArrayConfig : charArrayConfigs) {
            char[] value = charArrayConfig.get(key);
            if (value != null) {
                return value;
            }
        }

        return null;
    }

    public char[] getCharArray(String key, char[] defaultValue) {
        char[] value = getCharArray(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
