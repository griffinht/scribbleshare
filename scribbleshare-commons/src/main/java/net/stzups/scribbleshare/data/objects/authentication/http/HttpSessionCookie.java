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
import net.stzups.scribbleshare.data.objects.exceptions.DeserializationException;
import net.stzups.scribbleshare.server.http.HttpUtils;
import net.stzups.scribbleshare.server.http.exception.exceptions.BadRequestException;

import java.nio.charset.StandardCharsets;
import java.util.Set;

public class HttpSessionCookie {
    private final long id;
    private final byte[] token;

    HttpSessionCookie(ByteBuf byteBuf, int tokenLength) throws DeserializationException {
        try {
            id = byteBuf.readLong();
            token = new byte[tokenLength];
            byteBuf.readBytes(token);
        } catch (IndexOutOfBoundsException e) {
            throw new DeserializationException("Exception while deserializing HttpSessionCookie", e);
        }
    }

    public long getId() {
        return id;
    }

    public byte[] getToken() {
        return token;
    }

    private static void setCookie(HttpConfig config, DefaultCookie cookie) {
        cookie.setDomain(config.getDomain());
        if (config.getSSL()) cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setSameSite(CookieHeaderNames.SameSite.Strict);
    }

    public void setCookie(HttpConfig config, String name, HttpHeaders headers, long id, byte[] token) {
        ByteBuf tokenBuffer = Unpooled.buffer();
        tokenBuffer.writeLong(id);
        tokenBuffer.writeBytes(token);
        ByteBuf tokenBase64 = Base64.encode(tokenBuffer);
        tokenBuffer.release();

        DefaultCookie cookie = new DefaultCookie(name, tokenBase64.toString(StandardCharsets.UTF_8));
        tokenBase64.release();

        setCookie(config, cookie);

        HttpUtils.setCookie(headers, cookie);
    }

    public static void clearCookie(HttpConfig config, String name, HttpHeaders headers) {
        DefaultCookie cookie = new DefaultCookie(name, "");

        setCookie(config, cookie);
        cookie.setMaxAge(0);

        HttpUtils.setCookie(headers, cookie);
    }

    public static HttpSessionCookie getCookie(HttpRequest request, String name, int tokenLength) throws BadRequestException {
        String cookiesHeader = request.headers().get(HttpHeaderNames.COOKIE);
        if (cookiesHeader == null)
            return null;

        Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookiesHeader);
        for (Cookie cookie : cookies) {
            if (!cookie.name().equals(name)) {
                continue;
            }

            ByteBuf tokenBase64 = Unpooled.wrappedBuffer(cookie.value().getBytes(StandardCharsets.UTF_8));
            ByteBuf token = Base64.decode(tokenBase64);
            tokenBase64.release();
            HttpSessionCookie httpSessionCookie;
            try {
                httpSessionCookie = new HttpSessionCookie(token, tokenLength);
            } catch (DeserializationException e) {
                throw new BadRequestException("Malformed cookie content", e);
            } finally {
                token.release();
            }
            return httpSessionCookie;
        }

        return null;
    }
}