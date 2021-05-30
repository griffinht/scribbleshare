package net.stzups.scribbleshare.data.objects.authentication.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import net.stzups.scribbleshare.data.objects.exceptions.DeserializationException;
import net.stzups.scribbleshare.server.http.exception.exceptions.BadRequestException;
import net.stzups.scribbleshare.util.DebugString;

public class HttpUserSessionCookie extends HttpSessionCookie {
    private static final String COOKIE_NAME = "session";

    public HttpUserSessionCookie(ByteBuf byteBuf) throws DeserializationException {
        super(byteBuf);
    }

    public HttpUserSessionCookie(long id, byte[] token) {
        super(id, token);
    }

    public void setCookie(HttpConfig config, HttpHeaders headers) {
        setCookie(config, COOKIE_NAME, headers);
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

    public static void clearCookie(HttpConfig config, HttpHeaders headers) {
        HttpSessionCookie.clearCookie(config, COOKIE_NAME, headers);
    }

    @Override
    public String toString() {
        return DebugString.get(HttpUserSessionCookie.class, super.toString())
                .toString();
    }
}
