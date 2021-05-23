package net.stzups.scribbleshare.server.http;

import net.stzups.scribbleshare.server.http.exception.exceptions.BadRequestException;
import net.stzups.scribbleshare.server.http.exception.exceptions.NotFoundException;

import java.io.File;
import java.util.regex.Pattern;

public class Route extends Query {


    private final String[] route;

    public Route(String uri) throws BadRequestException {
        super(uri);
        if (!path().startsWith("/"))
            throw new BadRequestException("Route must start with a /");

        String[] route = path().substring(1).split("/");
        if (route.length == 0) {
            this.route = new String[] {""};
        } else {
            this.route = route;
        }
    }

    /** get route at index */
    public String get(int index) throws BadRequestException {
        if (!length(index)) {
            throw new BadRequestException("Route not long enough");
        }

        return route[index];
    }

    /** get length */
    public int length() {
        return route.length;
    }

    /** true if length is long enough */
    public boolean length(int index) {
        return route.length >= index;
    }
}
