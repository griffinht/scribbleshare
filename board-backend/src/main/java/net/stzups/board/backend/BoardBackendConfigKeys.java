package net.stzups.board.backend;

import net.stzups.board.util.config.ConfigKey;
import net.stzups.board.util.config.OptionalConfigKey;

public class BoardBackendConfigKeys {
    public static ConfigKey<String> HTML_ROOT = new OptionalConfigKey<>("html.root", "html");
}
