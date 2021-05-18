package net.stzups.scribbleshare.data.objects.authentication.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.CookieHeaderNames;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.server.http.HttpUtils;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

public class HttpUserSession extends UserSession {
    private static final Duration MAX_AGE = Duration.ofDays(1);

    public static final String COOKIE_NAME = "session";

    protected HttpUserSession(long user) {
        super(user);
    }

    public HttpUserSession(HttpConfig config, User user, HttpHeaders headers) {
        super(user.getId());
        HttpUtils.setCookie(headers, getCookie(config));
    }

    public HttpUserSession(long id, Timestamp creation, Timestamp expiration, long userId, ByteBuf byteBuf) {
        super(id, creation, expiration, userId, byteBuf);
    }

    protected DefaultCookie getCookie(String name) {
        ByteBuf token = Unpooled.buffer();
        token.writeLong(getId());
        token.writeBytes(super.generateToken());
        ByteBuf tokenBase64 = Base64.encode(token);
        DefaultCookie cookie = new DefaultCookie(name, tokenBase64.toString(StandardCharsets.UTF_8));
        tokenBase64.release();
        token.release();
        return cookie;
    }

    protected Cookie getCookie(HttpConfig config) {
        DefaultCookie cookie = getCookie(COOKIE_NAME);
        cookie.setDomain(config.getDomain());
        if (config.getSSL()) cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setSameSite(CookieHeaderNames.SameSite.Strict);
        //session cookie, so no max age

        return cookie;
    }

    public void clearCookie(HttpConfig config, HttpHeaders headers) {
        Cookie cookie = getCookie(config);
        cookie.setMaxAge(0);//todo
        HttpUtils.setCookie(headers, cookie);
    }

    public static HttpSessionCookie getCookie(HttpRequest request) {
        return HttpSessionCookie.getHttpSessionCookie(request, COOKIE_NAME);
    }

    public boolean validate(HttpSessionCookie cookie) {
        return validate(cookie.getToken()) && Instant.now().isBefore(getCreated().toInstant().plus(MAX_AGE));
    }
}
