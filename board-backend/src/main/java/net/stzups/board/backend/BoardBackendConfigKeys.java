package net.stzups.board.backend;

import net.stzups.board.util.config.ConfigKey;
import net.stzups.board.util.config.OptionalConfigKey;
import net.stzups.board.util.config.RequiredConfigKey;

public class BoardBackendConfigKeys {
    public static final ConfigKey<String> HTML_ROOT = new OptionalConfigKey<>("html.root", "html");
    public static final ConfigKey<String> MIME_TYPES_FILE_PATH = new OptionalConfigKey<>("mimetypes.path", "mime.types");
    public static final ConfigKey<Integer> HTTP_CACHE_SECONDS = new OptionalConfigKey<>("http.cache.seconds", 0);
}
