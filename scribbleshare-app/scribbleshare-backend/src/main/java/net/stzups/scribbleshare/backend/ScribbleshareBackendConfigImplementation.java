package net.stzups.scribbleshare.backend;

import net.stzups.scribbleshare.ScribbleshareConfigImplementation;
import net.stzups.config.ConfigKey;
import net.stzups.config.OptionalConfigKey;

public class ScribbleshareBackendConfigImplementation extends ScribbleshareConfigImplementation implements ScribbleshareBackendConfig {
    private static final ConfigKey<String> HTML_ROOT = new OptionalConfigKey<>("html.root", "html");
    private static final ConfigKey<String> MIME_TYPES_FILE_PATH = new OptionalConfigKey<>("mimetypes.path", "mime.types");
    private static final ConfigKey<Integer> HTTP_CACHE_SECONDS = new OptionalConfigKey<>("http.cache.seconds", 0);
    private static final ConfigKey<String> DEBUG_JS_ROOT = new OptionalConfigKey<>("debug.js.root", "");

    @Override
    public String getHttpRoot() {
        return getString(HTML_ROOT);
    }

    @Override
    public int getHttpCacheSeconds() {
        return getInteger(HTTP_CACHE_SECONDS);
    }

    @Override
    public String getMimeTypesFilePath() {
        return getString(MIME_TYPES_FILE_PATH);
    }

    @Override
    public String getDebugJsRoot() {
        return getString(DEBUG_JS_ROOT);
    }

}
