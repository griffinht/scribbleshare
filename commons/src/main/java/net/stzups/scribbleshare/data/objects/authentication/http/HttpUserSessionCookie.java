package net.stzups.scribbleshare.data.objects.authentication.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import net.stzups.netty.http.HttpUtils;
import net.stzups.netty.http.exception.exceptions.BadRequestException;
import net.stzups.scribbleshare.data.objects.exceptions.DeserializationException;
import net.stzups.util.DebugString;

public class HttpUserSessionCookie extends HttpSessionCookie {
    private static final String COOKIE_NAME = "session";

    public HttpUserSessionCookie(ByteBuf byteBuf) throws DeserializationException {
        super(byteBuf);
    }

    public HttpUserSessionCookie(long id, byte[] token) {
        super(id, token);
    }

    public void setCookie(HttpConfig config, HttpHeaders headers) {
        HttpUtils.setCookie(headers, getCookie(config, COOKIE_NAME));
    }

    public static HttpUserSessionCookie getCookie(HttpRequest request) throws BadRequestException {
        ByteBuf byteBuf = HttpSessionCookie.getCookie(request, COOKIE_NAME);
        if (byteBuf != null) {
            try {
                return new HttpUserSessionCookie(byteBuf);
            } catch (DeserializationException e) {
                throw new BadRequestException("Malformed cookie", e);
            } finally {
                byteBuf.release();
            }
        }

        return null;
    }

    public static void clearCookie(HttpResponse response) {
        HttpSessionCookie.clearCookie(COOKIE_NAME, response);
    }

    @Override
    public String toString() {
        return DebugString.get(HttpUserSessionCookie.class, super.toString())
                .toString();
    }
}
