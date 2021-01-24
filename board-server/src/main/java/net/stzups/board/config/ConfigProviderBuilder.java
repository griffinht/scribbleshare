package net.stzups.board.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a {@link ConfigProvider} with several different configs.
 */
public class ConfigProviderBuilder {
    private List<StringConfig> stringConfig = new ArrayList<>();
    private List<CharArrayConfig> charArrayConfig = new ArrayList<>();

    public ConfigProviderBuilder() {}

    public ConfigProviderBuilder addConfig(StringConfig config) {
        stringConfig.add(config);
        return this;
    }

    public ConfigProviderBuilder addConfig(CharArrayConfig config) {
        charArrayConfig.add(config);
        return this;
    }

    public ConfigProvider build() {
        return new ConfigProvider(stringConfig, charArrayConfig);
    }
}
