package net.stzups.scribbleshare.data.objects.authentication.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import net.stzups.scribbleshare.data.database.databases.HttpSessionDatabase;
import net.stzups.scribbleshare.data.database.databases.UserDatabase;
import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.data.objects.authentication.AuthenticatedUserSession;
import net.stzups.scribbleshare.data.objects.authentication.AuthenticationResult;
import net.stzups.scribbleshare.data.objects.authentication.UserSession;
import net.stzups.scribbleshare.server.http.exception.exceptions.BadRequestException;
import net.stzups.scribbleshare.server.http.exception.exceptions.InternalServerException;
import net.stzups.scribbleshare.server.http.exception.exceptions.UnauthorizedException;
import net.stzups.scribbleshare.util.DebugString;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

public class HttpUserSession extends UserSession {
    private static final Duration MAX_AGE = Duration.ofDays(1);

    protected HttpUserSession(long user) {
        super(user);
    }

    public HttpUserSession(HttpConfig config, User user, HttpResponse response) {
        super(user.getId());
        new HttpUserSessionCookie(getId(), generateToken()).setCookie(config, response.headers());
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
        return DebugString.get(HttpUserSession.class, super.toString())
                .toString();
    }


    public static <T extends HttpSessionDatabase> HttpUserSession getSession(T database, FullHttpRequest request) throws UnauthorizedException, InternalServerException {
        HttpUserSessionCookie cookie;
        try {
            cookie = HttpUserSessionCookie.getCookie(request);
        } catch (BadRequestException e) {
            throw new UnauthorizedException("Malformed cookie", e);
        }
        if (cookie == null) {
            return null;
        }

        HttpUserSession session;
        try {
            session = database.getHttpSession(cookie);
        } catch (DatabaseException e) {
            throw new InternalServerException(e);
        }
        if (session == null) {
            throw new UnauthorizedException("No " + HttpUserSession.class + " for " + cookie);
        }

        AuthenticationResult result = session.validate(cookie);
        if (result != AuthenticationResult.SUCCESS) {
            throw new UnauthorizedException("Validating " + cookie + " for " + session + " resulted in " + result);
        }

        return session;
    }


    /** authenticates, or null if no authentication */
    public static <T extends HttpSessionDatabase & UserDatabase> AuthenticatedUserSession authenticateHttpUserSession(T database, FullHttpRequest request) throws UnauthorizedException, InternalServerException {
        HttpUserSession session = getSession(database, request);
        if (session == null) {
            return null;
        }

        User user;
        try {
            user = database.getUser(session.getUser());
        } catch (DatabaseException e) {
            throw new InternalServerException(e);
        }

        if (user == null) {
            throw new InternalServerException("Unknown user for authentication");
        }
        return new AuthenticatedUserSession(user);
    }
}
