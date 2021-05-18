package net.stzups.scribbleshare.data.objects.authentication.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.CookieHeaderNames;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import net.stzups.scribbleshare.data.objects.User;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.Set;

public class HttpUserSession extends UserSession {
    public static class ClientCookie {
        private final long id;
        private final byte[] token;

        ClientCookie(ByteBuf byteBuf) {
            id = byteBuf.readLong();
            token = new byte[UserSession.TOKEN_LENGTH];
            byteBuf.readBytes(token);
        }

        public long getId() {
            return id;
        }

        public byte[] getToken() {
            return token;
        }

        public static ClientCookie getClientCookie(HttpRequest request, String name) {
            String cookiesHeader = request.headers().get(HttpHeaderNames.COOKIE);
            if (cookiesHeader != null) {
                Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookiesHeader);
                for (Cookie cookie : cookies) {
                    if (cookie.name().equals(name)) {
                        ByteBuf tokenBase64 = Unpooled.wrappedBuffer(cookie.value().getBytes(StandardCharsets.UTF_8));
                        ByteBuf token = Base64.decode(tokenBase64);
                        tokenBase64.release();
                        ClientCookie c = new ClientCookie(token);
                        token.release();
                        return c;
                    }
                }
            }
            return null;
        }
    }

    public static final String COOKIE_NAME = "session";
    private static final Duration AGE = Duration.ofDays(1);

    protected HttpUserSession(long user, TemporalAmount age) {
        super(user, age);
    }

    public HttpUserSession(HttpConfig config, User user, HttpHeaders headers) {
        super(user.getId(), AGE);
        setCookie(config, headers);
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

    private void setCookie(HttpConfig config, HttpHeaders headers) {
        DefaultCookie cookie = getCookie(COOKIE_NAME);
        cookie.setDomain(config.getDomain());
        //not used cookie.setPath("");
        if (config.getSSL()) cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setSameSite(CookieHeaderNames.SameSite.Strict);
        //session cookie, so no max age
        headers.add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
    }
}
