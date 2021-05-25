package net.stzups.scribbleshare.data.objects.authentication.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import net.stzups.scribbleshare.data.objects.exceptions.DeserializationException;
import net.stzups.scribbleshare.server.http.exception.exceptions.BadRequestException;

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

    public static HttpUserSessionCookie getHttpUserSessionCookie(HttpRequest request) throws BadRequestException {
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
}
