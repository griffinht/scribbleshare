package net.stzups.scribbleshare.data.objects.authentication.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

import java.nio.charset.StandardCharsets;
import java.util.Set;

public class HttpSessionCookie {
    private final long id;
    private final byte[] token;

    HttpSessionCookie(ByteBuf byteBuf) {
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

    public static HttpSessionCookie getHttpSessionCookie(HttpRequest request, String name) {
        String cookiesHeader = request.headers().get(HttpHeaderNames.COOKIE);
        if (cookiesHeader != null) {
            Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookiesHeader);
            for (Cookie cookie : cookies) {
                if (cookie.name().equals(name)) {
                    ByteBuf tokenBase64 = Unpooled.wrappedBuffer(cookie.value().getBytes(StandardCharsets.UTF_8));
                    ByteBuf token = Base64.decode(tokenBase64);
                    tokenBase64.release();
                    HttpSessionCookie c = new HttpSessionCookie(token);
                    token.release();
                    return c;
                }
            }
        }
        return null;
    }
}