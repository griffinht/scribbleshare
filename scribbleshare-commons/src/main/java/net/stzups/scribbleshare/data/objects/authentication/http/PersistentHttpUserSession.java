package net.stzups.scribbleshare.data.objects.authentication.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.cookie.CookieHeaderNames;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

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
        setCookie(config, headers);
    }

    public PersistentHttpUserSession(long id, Timestamp creation, Timestamp expiration, long userId, ByteBuf byteBuf) {
        super(id, creation, expiration, userId, byteBuf);
    }

    private void setCookie(HttpConfig config, HttpHeaders headers) {
        DefaultCookie cookie = getCookie(COOKIE_NAME);
        cookie.setDomain(config.getDomain());
        cookie.setPath("/");
        if (config.getSSL()) cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setSameSite(CookieHeaderNames.SameSite.Strict);
        cookie.setMaxAge(MAX_AGE.get(ChronoUnit.SECONDS)); //persistent cookie

        headers.add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
    }

    public static HttpSessionCookie getCookie(HttpRequest request) {
        return HttpSessionCookie.getHttpSessionCookie(request, COOKIE_NAME);
    }

    @Override
    public boolean validate(HttpSessionCookie cookie) {
        return validate(cookie.getToken()) && Instant.now().isBefore(getCreated().toInstant().plus(MAX_AGE));
    }
}
