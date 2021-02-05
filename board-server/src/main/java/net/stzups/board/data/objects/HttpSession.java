package net.stzups.board.data.objects;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

import java.io.Serializable;
import java.net.InetAddress;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HttpSession implements Serializable {
    private static final int SESSION_TOKEN_LENGTH = 16;
    private static final long SESSION_TOKEN_MAX_AGE = 2314;

    private static SecureRandom secureRandom = new SecureRandom();

    private static Map<String, HttpSession> sessions = new HashMap<>();


    public static Cookie getCookie(HttpHeaders httpHeaders, InetAddress address) {
        HttpSession httpSession = getSession(httpHeaders, address);
        if (httpSession == null) {
            return new HttpSession(address).regenerate();
        }
        return httpSession.regenerate();
    }

    public static HttpSession getSession(HttpHeaders httpHeaders, InetAddress address) {
        String cookieString = httpHeaders.get(HttpHeaderNames.COOKIE);
        if (cookieString != null) {
            Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieString);
            for (Cookie cookie : cookies) {
                if (cookie.name().equals("session-token")) {
                    String sessionToken = cookie.value();
                    HttpSession httpSession = sessions.get(sessionToken);
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

    private String sessionToken;
    private InetAddress address;

    private HttpSession(InetAddress address) {
        this.address = address;
    }

    private Cookie regenerate() {
        sessions.remove(sessionToken);
        byte[] bytes = new byte[SESSION_TOKEN_LENGTH];
        secureRandom.nextBytes(bytes);
        sessionToken = Base64.getEncoder().encodeToString(bytes);
        Cookie cookie = new DefaultCookie("session-token", sessionToken);
        cookie.setHttpOnly(true);
        cookie.setDomain("localhost");//todo
        cookie.setMaxAge(SESSION_TOKEN_MAX_AGE);
        cookie.setPath("index.html");//todo
        //cookie.setSecure(true); cant be done over http
        cookie.setWrap(true);//todo
        sessions.put(sessionToken, this);
        return cookie;
    }

    private boolean validate(Cookie cookie, InetAddress address) {
        return this.address.equals(address);
    }
}
