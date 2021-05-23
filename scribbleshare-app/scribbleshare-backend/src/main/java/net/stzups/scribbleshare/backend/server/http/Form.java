package net.stzups.scribbleshare.backend.server.http;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class Form {
    private final Map<String, String> form;

    public Form(FullHttpRequest request) throws BadRequestException {
        String contentType = request.headers().get(HttpHeaderNames.CONTENT_TYPE);

        if (contentType == null)
            throw new BadRequestException("Missing " + HttpHeaderNames.CONTENT_TYPE + " header");

        if (!contentType.contentEquals(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED))
            throw new BadRequestException(HttpHeaderNames.CONTENT_TYPE + " header is not " + HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED);

        //todo check host/origin/referer to make sure they originate from LOGIN_PAGE

        this.form = HttpServerHandler.parseQuery(request.content().toString(StandardCharsets.UTF_8));
    }
}
