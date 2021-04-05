package net.stzups.board.util.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a {@link Config} with several different configs.
 */
public class ConfigBuilder {
    private List<ConfigProvider> configProviders = new ArrayList<>();

    public ConfigBuilder() {}

    public ConfigBuilder addConfig(ConfigProvider configProvider) {
        configProviders.add(configProvider);
        return this;
    }

    public Config build() {
        return new Config(configProviders);
    }
}
