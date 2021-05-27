package net.stzups.scribbleshare.data.objects.authentication.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.data.objects.authentication.AuthenticationResult;
import net.stzups.scribbleshare.data.objects.authentication.UserSession;
import net.stzups.scribbleshare.util.DebugString;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

public class HttpUserSession extends UserSession {
    private static final Duration MAX_AGE = Duration.ofDays(1);

    protected HttpUserSession(long user) {
        super(user);
    }

    public HttpUserSession(HttpConfig config, User user, HttpHeaders headers) {
        super(user.getId());
        new HttpUserSessionCookie(getId(), generateToken()).setCookie(config, headers);
    }

    public HttpUserSession(long id, Timestamp creation, Timestamp expiration, long userId, ByteBuf byteBuf) {
        super(id, creation, expiration, userId, byteBuf);
    }

    public AuthenticationResult validate(HttpSessionCookie cookie) {
        if (!Instant.now().isBefore(getCreated().toInstant().plus(MAX_AGE)))
            return AuthenticationResult.STALE;

        return validate(cookie.getToken());
    }

    @Override
    public String toString() {
        return DebugString.get(HttpUserSession.class)
                .add(super.toString())
                .toString();
    }
}
