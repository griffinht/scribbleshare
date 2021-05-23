package net.stzups.scribbleshare.backend.server.http.exception;

import io.netty.handler.codec.http.HttpResponseStatus;

public abstract class HttpException extends Exception {
    protected HttpException(String message) {
        super(message);
    }

    protected HttpException(String message, HttpException cause) {
        super(message, cause);
    }

    public abstract HttpResponseStatus responseStatus();
}
