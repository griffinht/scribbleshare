package net.stzups.board.util.config.configs;

import net.stzups.board.util.config.ConfigProvider;

import java.util.Objects;

public class EnvironmentVariableConfig implements ConfigProvider {
    private final String prefix;

    public EnvironmentVariableConfig(String prefix) {
        this.prefix = Objects.requireNonNullElse(prefix, "");
    }

    @Override
    public String get(String key) {
        return System.getenv(prefix + key);
    }
}
