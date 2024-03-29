package net.stzups.scribbleshare.data.objects.authentication.http;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.CookieHeaderNames;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;

public class PersistentHttpSession extends HttpSession {
    private static final TemporalAmount MAX_SESSION_AGE = Duration.ofDays(90);//todo

    public static final String COOKIE_NAME = "persistent_session";
    public static final String LOGIN_PATH = "/";

    public PersistentHttpSession(long id, long user, Timestamp creation, byte[] hashedToken) {
        super(id, user, creation, hashedToken);
    }

    public PersistentHttpSession(HttpConfig config, HttpSession httpSession, HttpHeaders headers) {
        super(httpSession.getUser());
        setCookie(config, headers);
    }

    private void setCookie(HttpConfig config, HttpHeaders headers) {
        DefaultCookie cookie = getCookie(COOKIE_NAME);
        cookie.setDomain(config.getDomain());
        cookie.setPath(LOGIN_PATH);
        if (config.getSSL()) cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setSameSite(CookieHeaderNames.SameSite.Strict);
        cookie.setMaxAge(MAX_SESSION_AGE.get(ChronoUnit.SECONDS)); //persistent cookie

        headers.add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
    }

    @Override
    public boolean validate(byte[] token) {
        if (Instant.now().isAfter(getCreation().toInstant().plus(MAX_SESSION_AGE))) {
            return false;
        }
        return super.validate(token);
    }
}
