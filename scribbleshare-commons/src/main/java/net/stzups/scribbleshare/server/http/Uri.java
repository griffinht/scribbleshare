package net.stzups.scribbleshare.server.http;

import net.stzups.scribbleshare.server.http.exception.exceptions.BadRequestException;

import java.io.File;
import java.util.regex.Pattern;

public class Uri {
    // abc-ABC_123.file
    private static final String FILE_NAME_REGEX = "a-zA-Z0-9-_";
    private static final Pattern ALLOWED_CHARACTERS = Pattern.compile("^[/." + Query.QUERY_REGEX + FILE_NAME_REGEX + "]+$");

    final String uri;

    public Uri(String uri) throws BadRequestException {
        this.uri = getUri(uri);
    }

    public String getUri() {
        return uri;
    }

    /** Sanitizes uri */
    public static String getUri(String uri) throws BadRequestException {
        if (!ALLOWED_CHARACTERS.matcher(uri).matches())
            throw new BadRequestException("URI contains illegal characters");

        return uri;
    }

    private static final Pattern ALLOWED_PATH = Pattern.compile("^[\\\\" + File.separator + "." + FILE_NAME_REGEX + "]+$");

    private static String[] getRoute(String path) throws BadRequestException {
        if (!path.startsWith("/"))
            throw new BadRequestException("Route must start with a /");

        String[] route = path.substring(1).split("/");
        if (route.length == 0) {
            return new String[] {""};
        } else {
            return route;
        }
    }

    public boolean equals(String string) {
        return uri.equals(string);
    }
}
