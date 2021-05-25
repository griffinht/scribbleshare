package net.stzups.scribbleshare.data.objects.authentication.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.data.objects.authentication.AuthenticatedUserSession;
import net.stzups.scribbleshare.data.objects.authentication.AuthenticationException;
import net.stzups.scribbleshare.data.objects.authentication.UserSession;

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

    public AuthenticatedUserSession validate(HttpSessionCookie cookie) throws AuthenticationException {
        if (!Instant.now().isBefore(getCreated().toInstant().plus(MAX_AGE)))
            throw new AuthenticationException("Session is expired");

        validate(cookie.getToken());

        return new AuthenticatedUserSession(getUser());
    }
}
