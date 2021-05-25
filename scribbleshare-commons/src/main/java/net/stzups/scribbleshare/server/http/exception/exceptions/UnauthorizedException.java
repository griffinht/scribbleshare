package net.stzups.scribbleshare.server.http.exception.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import net.stzups.scribbleshare.server.http.exception.HttpException;

public class UnauthorizedException extends HttpException {
    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Exception e) {
        super(message, e);
    }

    @Override
    public HttpResponseStatus responseStatus() {
        return HttpResponseStatus.UNAUTHORIZED;
    }
}
