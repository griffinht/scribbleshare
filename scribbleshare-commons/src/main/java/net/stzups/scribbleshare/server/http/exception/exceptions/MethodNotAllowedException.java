package net.stzups.scribbleshare.server.http.exception.exceptions;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import net.stzups.scribbleshare.server.http.exception.HttpException;

public class MethodNotAllowedException extends HttpException {

    public MethodNotAllowedException(HttpMethod actual) {
        super("Bad method " + actual);
    }

    public MethodNotAllowedException(HttpMethod actual, HttpMethod expected) {
        super("Bad method " + actual + ", should have been " + expected);
    }

    @Override
    public HttpResponseStatus responseStatus() {
        return HttpResponseStatus.METHOD_NOT_ALLOWED;
    }
}
