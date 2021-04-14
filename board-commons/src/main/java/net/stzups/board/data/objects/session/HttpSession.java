package net.stzups.board.data.objects.session;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.CookieHeaderNames;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import net.stzups.board.data.objects.User;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Set;

public class HttpSession extends Session {
    public static class ClientCookie {
        private long id;
        private long token;

        ClientCookie(long id, long token) {
            this.id = id;
            this.token = token;
        }

        public long getId() {
            return id;
        }

        public long getToken() {
            return token;
        }

        public static ClientCookie getClientCookie(HttpRequest request, String name) {
            String cookiesHeader = request.headers().get(HttpHeaderNames.COOKIE);
            if (cookiesHeader != null) {
                Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookiesHeader);
                for (Cookie cookie : cookies) {
                    if (cookie.name().equals(name)) {
                        ByteBuf byteBuf = Base64.decode(Unpooled.wrappedBuffer(cookie.value().getBytes(StandardCharsets.UTF_8)));
                        return new ClientCookie(byteBuf.readLong(), byteBuf.readLong());
                    }
                }
            }
            return null;
        }
    }

    public static final String COOKIE_NAME = "session";

    public HttpSession(long user) {
        super(user);
    }

    public HttpSession(long id, long user, Timestamp creation, byte[] hashedToken) {
        super(id, user, creation, hashedToken);
    }

    public HttpSession(User user, HttpResponse response) {
        super(user.getId());
        setCookie(response);
    }

    public HttpSession(long id, ByteBuf byteBuf) {
        super(id, byteBuf);
    }

    protected DefaultCookie getCookie(String name) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeLong(getId());
        byteBuf.writeLong(super.generateToken());
        return new DefaultCookie(name, Base64.encode(byteBuf).toString(StandardCharsets.UTF_8));
    }

    private void setCookie(HttpResponse response) {
        DefaultCookie cookie = getCookie(COOKIE_NAME);
        cookie.setDomain("localhost");
        //not used cookie.setPath("");
        //todo ssl cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setSameSite(CookieHeaderNames.SameSite.Strict);
        //session cookie

        response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
    }
}
