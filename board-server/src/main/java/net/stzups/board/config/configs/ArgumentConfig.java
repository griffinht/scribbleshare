package net.stzups.board.config.configs;

import net.stzups.board.config.StringConfig;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ArgumentConfig implements StringConfig {
    private Map<String, String> flags = new HashMap<>();

    public ArgumentConfig(String[] args) {
        Iterator<String> iterator = Arrays.asList(args).iterator();
        // format:
        // --flag value
        // --flag "value with space"
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
