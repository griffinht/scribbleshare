package net.stzups.scribbleshare.backend.server.http.exception.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import net.stzups.scribbleshare.backend.server.http.exception.HttpException;

public class UnauthorizedException extends HttpException {
    public UnauthorizedException(String message) {
        super(message);
    }

    @Override
    public HttpResponseStatus responseStatus() {
        return HttpResponseStatus.UNAUTHORIZED;
    }
}
