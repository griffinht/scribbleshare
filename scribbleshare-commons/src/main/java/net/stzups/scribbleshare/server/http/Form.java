package net.stzups.scribbleshare.server.http;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import net.stzups.scribbleshare.server.http.exception.exceptions.BadRequestException;

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

        this.form = Query.parseQuery(request.content().toString(StandardCharsets.UTF_8));
    }

    public String getText(String field) throws BadRequestException {
        String value = form.get(field);
        if (value == null) {
            throw new BadRequestException("Missing value for " + field);
        }

        return value;
    }

    public boolean getCheckbox(String field) throws BadRequestException {
        String value = form.get(field);
        if (value == null) { //http forms don't include false
            return false;
        } else if (value.equals("on")) {
            return true;
        } else {
            throw new BadRequestException("Malformed value for " + field);
        }
    }
}
