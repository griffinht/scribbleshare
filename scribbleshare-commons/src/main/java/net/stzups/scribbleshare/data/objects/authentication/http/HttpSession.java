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
import java.util.Set;

public class HttpSession extends Session {
    public static class ClientCookie {
        private final long id;
        private final byte[] token;

        ClientCookie(ByteBuf byteBuf) {
            id = byteBuf.readLong();
            token = new byte[16];//todo
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
                        ByteBuf b = Unpooled.wrappedBuffer(cookie.value().getBytes(StandardCharsets.UTF_8));
                        ByteBuf byteBuf = Base64.decode(b);
                        b.release();
                        ClientCookie c = new ClientCookie(byteBuf);
                        byteBuf.release();
                        return c;
                    }
                }
            }
            return null;
        }
    }

    public static final String COOKIE_NAME = "session";

    protected HttpSession(long user) {
        super(user);
    }

    protected HttpSession(long id, long user, Timestamp creation, byte[] hashedToken) {
        super(id, user, creation, hashedToken);
    }

    public HttpSession(HttpConfig config, User user, HttpHeaders headers) {
        super(user.getId());
        setCookie(config, headers);
    }

    public HttpSession(long id, ByteBuf byteBuf) {
        super(id, byteBuf);
    }

    protected DefaultCookie getCookie(String name) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeLong(getId());
        byteBuf.writeBytes(super.generateToken());
        return new DefaultCookie(name, Base64.encode(byteBuf).toString(StandardCharsets.UTF_8));
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
