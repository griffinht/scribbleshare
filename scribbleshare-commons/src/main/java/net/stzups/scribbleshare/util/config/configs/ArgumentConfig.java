package net.stzups.scribbleshare.util.config.configs;

import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.util.config.ConfigProvider;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Takes arguments from the console and parses them as key value pairs.
 */
public class ArgumentConfig implements ConfigProvider {
    private final Map<String, String> flags = new HashMap<>();

    /**
     * Format:
     * --flag value
     * --flag "value with space"
     * Flags that are not properly formatted will be ignored.
     * @param args should be take from program entry point, in the proper format
     */
    public ArgumentConfig(String[] args) {
        Iterator<String> iterator = Arrays.asList(args).iterator();
        while (iterator.hasNext()) {
            String raw = iterator.next();
            String[] split = raw.split("=");
            if (split.length == 2) {
                String value = split[1];
                if (value.startsWith("\"")) {
                    value = value.substring(1);
                    while (iterator.hasNext() && !value.endsWith("\"")) {
                        value += iterator.next();
                    }
                    if (value.endsWith("\"")) {
                        value = value.substring(0, value.length() - 1);
                    } else {
                        Scribbleshare.getLogger().warning("Malformed argument " + raw + " and " + value + ", should be formatted --key=value or --key=\"value with spaces\"");
                        continue;
                    }
                }
                flags.put(split[0].substring(2), value);
            } else {
                Scribbleshare.getLogger().warning("Malformed argument " + raw + ", should be formatted --key=value or --key=\"value with spaces\"");
            }
        }
    }

    @Override
    public String get(String key) {
        return flags.get(key);
    }
}
