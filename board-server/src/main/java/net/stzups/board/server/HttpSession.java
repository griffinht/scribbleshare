package net.stzups.board.server;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

import java.net.InetAddress;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HttpSession {
    private static final int SESSION_TOKEN_LENGTH = 16;
    private static final long SESSION_TOKEN_MAX_AGE = 2314;

    private static SecureRandom secureRandom = new SecureRandom();

    private static Map<String, HttpSession> sessions = new HashMap<>();


    public static HttpSession getSession(FullHttpRequest fullHttpRequest, InetAddress address) {
        String cookieString = fullHttpRequest.headers().get(HttpHeaderNames.COOKIE);
        if (cookieString != null) {
            Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieString);
            for (Cookie cookie : cookies) {
                if (cookie.name().equals("session-token")) {
                    String sessionToken = cookie.value();
                    HttpSession httpSession = sessions.remove(sessionToken);
                    if (httpSession != null && httpSession.validate(cookie, address)) {
                        httpSession.regenerate();
                        return httpSession;//good session
                    }
                    //bad session
                    break;
                }
            }
        }
        //new session
        return new HttpSession(address);
    }

    private String sessionToken;
    private InetAddress address;
    private Cookie cookie;

    private HttpSession(InetAddress address) {
        this.address = address;
        regenerate();
    }

    Cookie getCookie() {
        return cookie;
    }

    private void regenerate() {
        byte[] bytes = new byte[SESSION_TOKEN_LENGTH];
        secureRandom.nextBytes(bytes);
        sessionToken = Base64.getEncoder().encodeToString(bytes);
        cookie = new DefaultCookie("session-token", sessionToken);
        cookie.setHttpOnly(true);
        cookie.setDomain("localhost");//todo
        cookie.setMaxAge(SESSION_TOKEN_MAX_AGE);
        cookie.setPath("index.html");//todo
        //cookie.setSecure(true); cant be done over http
        cookie.setWrap(true);//todo
        System.out.println(cookie.value());
        System.out.println(cookie.toString());
        System.out.println("add " + sessionToken);
        sessions.put(sessionToken, this);
    }

    private boolean validate(Cookie cookie, InetAddress address) {
        return this.address.equals(address);
    }
}
