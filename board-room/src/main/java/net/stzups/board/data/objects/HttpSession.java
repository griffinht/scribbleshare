package net.stzups.board.data.objects;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import net.stzups.board.BoardRoom;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class HttpSession {
    private static final long SESSION_TOKEN_MAX_AGE = 2314;//todo

    public static Cookie getCookie(HttpHeaders httpHeaders, InetAddress address) {
        HttpSession httpSession = getSession(httpHeaders, address);
        if (httpSession == null) {
            return new HttpSession(address).generate();
        }
        return null;
    }

    public static HttpSession getSession(HttpHeaders httpHeaders, InetAddress address) {
        String cookieString = httpHeaders.get(HttpHeaderNames.COOKIE);
        if (cookieString != null) {
            Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieString);
            for (Cookie cookie : cookies) {
                if (cookie.name().equals("session-token")) {
                    HttpSession httpSession = null;//BoardRoom.getDatabase().getHttpSession(Base64.decode(Unpooled.wrappedBuffer(cookie.value().getBytes())).getLong(0));todo
                    if (httpSession != null && httpSession.validate(cookie, address)) {
                        System.out.println("good session");
                        return httpSession;
                    }
                    //bad session
                    System.out.println("bad session");
                    break;
                }
            }
        }
        //new session
        System.out.println("no session");
        return null;
    }

    private long token;
    private InetAddress address;

    private HttpSession(InetAddress address) {
        this.address = address;
    }

    private Cookie generate() {//todo make sure this is only called once
        token = BoardRoom.getSecureRandom().nextLong();
        Cookie cookie = new DefaultCookie("session-token", Base64.encode(Unpooled.copyLong(token)).toString(StandardCharsets.US_ASCII));//todo allocation
        cookie.setHttpOnly(true);
        cookie.setDomain("localhost");//todo
        cookie.setMaxAge(SESSION_TOKEN_MAX_AGE);
        cookie.setPath("/");//todo
        //cookie.setSecure(true); cant be done over http
        cookie.setWrap(true);//todo
        //BoardRoom.getDatabase().addHttpSession(this);todo
        return cookie;
    }

    public long getToken() {
        return token;
    }

    private boolean validate(Cookie cookie, InetAddress address) {
        return this.address.equals(address);
    }

    @Override
    public String toString() {
        return "HttpSession{address=" + address + "}";
    }

    @Override
    public int hashCode() {
        return Long.hashCode(token);
    }
}
