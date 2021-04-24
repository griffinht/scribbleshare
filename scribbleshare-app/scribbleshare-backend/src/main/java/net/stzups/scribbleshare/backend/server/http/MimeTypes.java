package net.stzups.scribbleshare.backend.server.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple utility class parses a mime.types file from resources and provides MIME type mappings.
 * mime.types should be formatted as follows:
 * text/html htm html
 * image/jpeg jpg jpeg
 * application/javascript js
 */
public class MimeTypes {
    private static final Map<String, String> extensionMimeTypeMap = new HashMap<>();

    static void load(InputStream inputStream) throws IOException {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            for (String line; (line = bufferedReader.readLine()) != null;) {
                String[] split = line.split("\\s");
                if (split.length > 1) {
                    for (int i = 1; i < split.length; i++) {
                        extensionMimeTypeMap.put(split[i], split[0]);
                    }
                }
            }
        } catch (IOException e) {
            throw new IOException("Exception while loading MIME types from InputStream", e);
        }
    }

    /**
     * Gets a MIME type for the extension from the path of a {@link java.io.File}, or the default mime type if the extension is unknown.
     *
     * @return the MIME type for the given file, or a default type
     */
    public static String getMimeType(File file) {
        return getMimeType(file.getPath());
    }

    /**
     * Gets a MIME type for an extension.
     *
     * @param extension can include or exclude the file separator ("html" or ".html" both work)
     * @return the MIME type for the given extension, or null
     */
    public static String getMimeType(String extension) {
        int i = extension.lastIndexOf(".");
        if (i != -1) {
            extension = extension.substring(i + 1);
        }
        return extensionMimeTypeMap.get(extension);
    }
}