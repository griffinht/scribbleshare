package net.stzups.scribbleshare.backend;

import net.stzups.scribbleshare.util.config.ConfigKey;
import net.stzups.scribbleshare.util.config.OptionalConfigKey;

public class ScribbleshareBackendConfigKeys {
    public static final ConfigKey<String> HTML_ROOT = new OptionalConfigKey<>("html.root", "html");
    public static final ConfigKey<String> MIME_TYPES_FILE_PATH = new OptionalConfigKey<>("mimetypes.path", "mime.types");
    public static final ConfigKey<Integer> HTTP_CACHE_SECONDS = new OptionalConfigKey<>("http.cache.seconds", 0);
}
