package net.stzups.scribbleshare.server.http.exception.exceptions;


import io.netty.handler.codec.http.HttpResponseStatus;
import net.stzups.scribbleshare.server.http.exception.HttpException;

public class InternalServerException extends HttpException {
    public InternalServerException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public HttpResponseStatus responseStatus() {
        return HttpResponseStatus.INTERNAL_SERVER_ERROR;
    }
}
