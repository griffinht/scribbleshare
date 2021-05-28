package net.stzups.scribbleshare.server.http.objects;

import net.stzups.scribbleshare.server.http.exception.exceptions.BadRequestException;
import net.stzups.scribbleshare.util.DebugString;

import java.util.regex.Pattern;

public class Uri {
    // abc-ABC_123.file
    public static final String FILE_NAME_REGEX = "a-zA-Z0-9-";
    private static final Pattern ALLOWED_CHARACTERS = Pattern.compile("^[/." + Query.QUERY_REGEX + FILE_NAME_REGEX + "]+$");

    final String uri;

    public Uri(String uri) throws BadRequestException {
        if (!ALLOWED_CHARACTERS.matcher(uri).matches())
            throw new BadRequestException("URI contains illegal characters");

        this.uri = uri;
    }

    public String uri() {
        return uri;
    }

    public boolean equals(String string) {
        return uri.equals(string);
    }

    @Override
    public String toString() {
        return DebugString.get(Uri.class)
                .add("uri", uri)
                .toString();
    }
}
