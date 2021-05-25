package net.stzups.scribbleshare.data.objects.authentication.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import net.stzups.scribbleshare.data.objects.authentication.AuthenticationException;
import net.stzups.scribbleshare.data.objects.authentication.UserSession;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

public class PersistentHttpUserSession extends UserSession {
    private static final Duration MAX_AGE = Duration.ofDays(90);

    public static final String LOGIN_PATH = "/login";

    public PersistentHttpUserSession(HttpConfig config, HttpUserSession httpSession, HttpHeaders headers) {
        super(httpSession.getUser());
        new PersistentHttpUserSessionCookie(getId(), generateToken()).setCookie(config, headers);
    }

    public PersistentHttpUserSession(long id, Timestamp creation, Timestamp expiration, long userId, ByteBuf byteBuf) {
        super(id, creation, expiration, userId, byteBuf);
    }

    public void validate(HttpSessionCookie cookie) throws AuthenticationException {
        if (!Instant.now().isBefore(getCreated().toInstant().plus(MAX_AGE)))
            throw new AuthenticationException("Session is too old");

        validate(cookie.getToken());
    }
}
