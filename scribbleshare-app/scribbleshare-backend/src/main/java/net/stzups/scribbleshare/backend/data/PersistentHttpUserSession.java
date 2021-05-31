package net.stzups.scribbleshare.backend.data;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import net.stzups.scribbleshare.data.objects.authentication.AuthenticationResult;
import net.stzups.scribbleshare.data.objects.authentication.UserSession;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpConfig;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpSessionCookie;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSession;
import net.stzups.scribbleshare.util.DebugString;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

public class PersistentHttpUserSession extends UserSession {
    private static final Duration MAX_AGE = Duration.ofDays(90);

    public static final String LOGIN_PATH = "/";

    public PersistentHttpUserSession(HttpConfig config, HttpUserSession httpSession, HttpHeaders headers) {
        super(httpSession.getUser());
        new PersistentHttpUserSessionCookie(getId(), generateToken()).setCookie(config, headers);
    }

    public PersistentHttpUserSession(long id, Timestamp creation, Timestamp expiration, long userId, ByteBuf byteBuf) {
        super(id, creation, expiration, userId, byteBuf);
    }

    public AuthenticationResult validate(HttpSessionCookie cookie) {
        if (!Instant.now().isBefore(getCreated().toInstant().plus(MAX_AGE)))
            return AuthenticationResult.STALE;

        return validate(cookie.getToken());
    }

    @Override
    public String toString() {
        return DebugString.get(PersistentHttpUserSession.class, super.toString())
                .toString();
    }
}
