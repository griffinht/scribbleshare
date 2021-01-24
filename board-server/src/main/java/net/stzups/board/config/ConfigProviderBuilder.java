package net.stzups.board.config;

import java.util.ArrayList;
import java.util.List;

public class ConfigProviderBuilder {
    private List<Config> configs = new ArrayList<>();
    public ConfigProviderBuilder() {

    }

    public ConfigProviderBuilder addConfig(Config config) {
        configs.add(config);
        return this;
    }

    public ConfigProvider build() {
        return new ConfigProvider(configs);
    }
}
