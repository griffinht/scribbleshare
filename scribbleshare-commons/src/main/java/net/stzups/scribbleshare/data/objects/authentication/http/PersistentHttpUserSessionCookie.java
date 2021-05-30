package net.stzups.scribbleshare.data.objects.authentication.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import net.stzups.scribbleshare.data.objects.exceptions.DeserializationException;
import net.stzups.scribbleshare.server.http.exception.exceptions.BadRequestException;
import net.stzups.scribbleshare.util.DebugString;

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

    protected static void setCookie(HttpConfig config, DefaultCookie cookie) {
        HttpUserSessionCookie.setCookie(config, cookie);
        cookie.setMaxAge(MAX_AGE.get(ChronoUnit.SECONDS)); //persistent cookie
    }

    public void setCookie(HttpConfig config, HttpHeaders headers) {
        setCookie(config, COOKIE_NAME, headers);
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

    public static void clearCookie(HttpConfig config, HttpHeaders headers) {
        HttpSessionCookie.clearCookie(config, COOKIE_NAME, headers);
    }

    @Override
    public String toString() {
        return DebugString.get(PersistentHttpUserSessionCookie.class, super.toString())
                .toString();
    }
}
