package net.stzups.board.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple utility class that looks for a META-INF/mime.types file from resources and provides MIME type mappings.
 * mime.types should be formatted as follows:
 * text/html htm html
 * image/jpeg jpg jpeg
 * application/javascript js
 */
public class MimeTypes {
    private static final String MIME_TYPES_FILE_PATH = "/META-INF/mime.types";
    private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    private static Map<String, String> extensionMimeTypeMap = new HashMap<>();

    static {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(MimeTypes.class.getResourceAsStream(MIME_TYPES_FILE_PATH)));
            for (String line; (line = bufferedReader.readLine()) != null;) {
                String[] split = line.split("\\s");
                if (split.length > 1) {
                    for (int i = 1; i < split.length; i++) {
                        extensionMimeTypeMap.put(split[i], split[0]);
                    }
                }
            }
        } catch (IOException e) {
            new IOException("Exception while loading MIME types from mime.types resource", e).printStackTrace();
        }
    }

    /**
     * Gets a MIME type for the extension from the path of a {@link java.io.File}.
     *
     * @param file the file
     * @return the MIME type for the given file, or a default type
     */
    public static String getMimeTypeFromExtension(File file) {
        return getMimeTypeFromExtension(file.getPath());
    }

    /**
     * Gets a MIME type for an extension.
     *
     * @param extension the extension, can include or exclude the file separator ("html" or ".html" both work)
     * @return the MIME type for the given extension, or a default type
     */
    public static String getMimeTypeFromExtension(String extension) {
        int i = extension.lastIndexOf(".");
        if (i != -1) {
            extension = extension.substring(i + 1);
        }
        return extensionMimeTypeMap.getOrDefault(extension, DEFAULT_MIME_TYPE);
    }
}
