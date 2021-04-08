package net.stzups.board.backend;

import net.stzups.board.util.config.ConfigKey;
import net.stzups.board.util.config.OptionalConfigKey;

public class BoardBackendConfigKeys {
    public static final ConfigKey<String> HTML_ROOT = new OptionalConfigKey<>("html.root", "html");
    public static final ConfigKey<String> MIME_TYPES_FILE_PATH = new OptionalConfigKey<>("mimetypes.path", "mime.types");
}
