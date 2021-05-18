package net.stzups.scribbleshare.data.objects.authentication.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import net.stzups.scribbleshare.server.http.HttpUtils;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class PersistentHttpUserSession extends HttpUserSession {
    private static final Duration MAX_AGE = Duration.ofDays(90);

    public static final String COOKIE_NAME = "persistent_session";
    public static final String LOGIN_PATH = "/login";


    public PersistentHttpUserSession(HttpConfig config, HttpUserSession httpSession, HttpHeaders headers) {
        super(httpSession.getUser());
        HttpUtils.setCookie(headers, getCookie(config));
    }

    public PersistentHttpUserSession(long id, Timestamp creation, Timestamp expiration, long userId, ByteBuf byteBuf) {
        super(id, creation, expiration, userId, byteBuf);
    }

    private Cookie getCookie(HttpConfig config) {
        DefaultCookie cookie = getCookie(config, COOKIE_NAME);

        cookie.setMaxAge(MAX_AGE.get(ChronoUnit.SECONDS)); //persistent cookie

        return cookie;
    }

    public static HttpSessionCookie getCookie(HttpRequest request) {
        return HttpSessionCookie.getHttpSessionCookie(request, COOKIE_NAME);
    }

    @Override
    public boolean validate(HttpSessionCookie cookie) {
        return validate(cookie.getToken()) && Instant.now().isBefore(getCreated().toInstant().plus(MAX_AGE));
    }
}
