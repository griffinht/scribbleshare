package net.stzups.board.util.config.configs;

import net.stzups.board.util.config.ConfigProvider;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Takes arguments from the console and parses them as key value pairs.
 */
public class ArgumentConfig implements ConfigProvider {
    private Map<String, String> flags = new HashMap<>();

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
            String flag = iterator.next();
            if (flag.startsWith("--") && iterator.hasNext()) {
                String value = iterator.next();
                if (value.startsWith("\"")) {
                    value = value.substring(1);
                    while (iterator.hasNext() && !value.endsWith("\"")) {
                        value += iterator.next();
                    }
                    if (value.endsWith("\"")) {
                        value = value.substring(0, value.length() - 1);
                    }
                }
                flags.put(flag.substring(2), value);
            }
        }
    }

    @Override
    public String get(String key) {
        return flags.get(key);
    }
}
