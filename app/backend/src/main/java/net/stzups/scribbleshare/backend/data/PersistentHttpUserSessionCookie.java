package net.stzups.scribbleshare.backend.data;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.cookie.Cookie;
import net.stzups.netty.http.HttpUtils;
import net.stzups.netty.http.exception.exceptions.BadRequestException;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpConfig;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpSessionCookie;
import net.stzups.scribbleshare.data.objects.exceptions.DeserializationException;
import net.stzups.util.DebugString;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class PersistentHttpUserSessionCookie extends HttpSessionCookie {
    private static final String COOKIE_NAME = "persistent_session";
    private static final Duration MAX_AGE = Duration.ofDays(90);

    PersistentHttpUserSessionCookie(ByteBuf byteBuf) throws DeserializationException {
        super(byteBuf);
    }

    PersistentHttpUserSessionCookie(long id, byte[] token) {
        super(id, token);
    }

  /*  @Override
    protected static void setCookie(HttpConfig config, DefaultCookie cookie) {

    }*/

    public void setCookie(HttpConfig config, HttpHeaders headers) {
        Cookie cookie = getCookie(config, COOKIE_NAME);
        cookie.setMaxAge(MAX_AGE.get(ChronoUnit.SECONDS)); //persistent cookie
        cookie.setPath("/login");//todo
        HttpUtils.setCookie(headers, cookie);
    }

    public static PersistentHttpUserSessionCookie getCookie(HttpRequest request) throws BadRequestException {
        ByteBuf byteBuf = HttpSessionCookie.getCookie(request, COOKIE_NAME);
        if (byteBuf != null) {
            try {
                return new PersistentHttpUserSessionCookie(byteBuf);
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
        return DebugString.get(PersistentHttpUserSessionCookie.class, super.toString())
                .toString();
    }
}
