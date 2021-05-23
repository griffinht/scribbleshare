package net.stzups.scribbleshare.backend.server.http.exception.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import net.stzups.scribbleshare.backend.server.http.exception.HttpException;

public class NotFoundException extends HttpException {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, HttpException cause) {
        super(message, cause);
    }

    @Override
    public HttpResponseStatus responseStatus() {
        return HttpResponseStatus.NOT_FOUND;
    }
}
