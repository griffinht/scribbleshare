package net.stzups.scribbleshare.backend.data;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import net.stzups.scribbleshare.backend.data.database.databases.PersistentHttpSessionDatabase;
import net.stzups.scribbleshare.data.database.databases.HttpSessionDatabase;
import net.stzups.scribbleshare.data.database.databases.UserDatabase;
import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.data.objects.authentication.AuthenticationResult;
import net.stzups.scribbleshare.data.objects.authentication.UserSession;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpConfig;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpSessionCookie;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSession;
import net.stzups.scribbleshare.server.http.exception.exceptions.BadRequestException;
import net.stzups.scribbleshare.server.http.exception.exceptions.InternalServerException;
import net.stzups.scribbleshare.server.http.exception.exceptions.UnauthorizedException;
import net.stzups.scribbleshare.util.DebugString;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

public class PersistentHttpUserSession extends UserSession {
    private static final Duration MAX_AGE = Duration.ofDays(90);

    public static final String LOGIN_PATH = "/";

    public PersistentHttpUserSession(HttpConfig config, HttpUserSession httpSession, HttpResponse response) {
        super(httpSession.getUser());
        new PersistentHttpUserSessionCookie(getId(), generateToken()).setCookie(config, response);
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

    /** get and expire persistent http user session */

    public static<T extends PersistentHttpSessionDatabase> PersistentHttpUserSession getSession(T database, FullHttpRequest request, HttpResponse response) throws UnauthorizedException, InternalServerException, BadRequestException {
        PersistentHttpUserSessionCookie cookie = PersistentHttpUserSessionCookie.getCookie(request);
        if (cookie == null) {
            return null;
        }

        PersistentHttpUserSession session;
        try {
            session = database.getPersistentHttpUserSession(cookie);

            if (session == null) {
                throw new UnauthorizedException("No " + PersistentHttpUserSession.class + " for " + cookie);
            }

            database.expirePersistentHttpUserSession(session);
            PersistentHttpUserSessionCookie.clearCookie(response);
        } catch (DatabaseException e) {
            throw new InternalServerException(e);
        }

        AuthenticationResult result = session.validate(cookie);
        if (result != AuthenticationResult.SUCCESS) {
            throw new UnauthorizedException("Validating " + cookie + " for " + session + " resulted in " + result);
        }

        return session;
    }

     /** logs in if not authenticated, or null if no auth */
    public static<T extends HttpSessionDatabase & PersistentHttpSessionDatabase & UserDatabase> HttpUserSession logIn(HttpConfig config, T database, FullHttpRequest request, HttpResponse response) throws UnauthorizedException, InternalServerException, BadRequestException {
        PersistentHttpUserSession session = getSession(database, request, response);
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
            throw new InternalServerException("User somehow does not exist " + user);
        }

        HttpUserSession s = new HttpUserSession(config, user, response);
        return createHttpSession(config, database, user, headers);
    }
}
