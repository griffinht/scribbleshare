package net.stzups.scribbleshare.server.http.handler.handlers;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.AttributeKey;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.data.database.databases.HttpSessionDatabase;
import net.stzups.scribbleshare.data.database.databases.PersistentHttpSessionDatabase;
import net.stzups.scribbleshare.data.database.databases.UserDatabase;
import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.data.objects.authentication.AuthenticatedUserSession;
import net.stzups.scribbleshare.data.objects.authentication.AuthenticationResult;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpConfig;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSession;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSessionCookie;
import net.stzups.scribbleshare.data.objects.authentication.http.PersistentHttpUserSession;
import net.stzups.scribbleshare.data.objects.authentication.http.PersistentHttpUserSessionCookie;
import net.stzups.scribbleshare.server.http.exception.HttpException;
import net.stzups.scribbleshare.server.http.exception.exceptions.BadRequestException;
import net.stzups.scribbleshare.server.http.exception.exceptions.InternalServerException;
import net.stzups.scribbleshare.server.http.exception.exceptions.NotFoundException;
import net.stzups.scribbleshare.server.http.exception.exceptions.UnauthorizedException;
import net.stzups.scribbleshare.server.http.handler.HttpHandler;

@ChannelHandler.Sharable
public class HttpAuthenticator extends HttpHandler {
    private static final AttributeKey<AuthenticatedUserSession> USER = AttributeKey.valueOf(HttpAuthenticator.class, "USER");
    public static AuthenticatedUserSession getUser(ChannelHandlerContext ctx) {
        return ctx.channel().attr(USER).get();
    }

    public interface Database extends HttpSessionDatabase, PersistentHttpSessionDatabase, UserDatabase {

    }

    private final Database database;
    private final String uri;

    public HttpAuthenticator(Database database) {
        this(database, null);
    }

    public HttpAuthenticator(Database database, String uri) {
        super("/");
        this.database = database;
        this.uri = uri;
    }

    public boolean handle(ChannelHandlerContext ctx, FullHttpRequest request) throws HttpException {
        if (uri != null && !request.uri().equals(uri)) {
            throw new NotFoundException("Bad uri");
        }

        AuthenticatedUserSession user = ctx.channel().attr(USER).get();
        if (user != null) {
            return true;
        }

        AuthenticatedUserSession session = authenticateHttpUserSession(database, request);
        if (session == null) {
            throw new UnauthorizedException("No authentication");
        }

        Scribbleshare.getLogger(ctx).info("" + session);
        ctx.channel().attr(USER).set(session);
        return true;
    }

    public static HttpUserSession getHttpUserSession(Database database, FullHttpRequest request) throws UnauthorizedException, InternalServerException {
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
            throw new UnauthorizedException("No " + PersistentHttpUserSession.class + " for " + cookie);
        }

        AuthenticationResult result = session.validate(cookie);
        if (result != AuthenticationResult.SUCCESS) {
            throw new UnauthorizedException("Validating " + cookie + " for " + session + " resulted in " + result);
        }

        return session;
    }

    /** get and expire persistent http user session */
    public static PersistentHttpUserSession getPersistentHttpUserSession(Database database, FullHttpRequest request) throws UnauthorizedException, InternalServerException {
        PersistentHttpUserSessionCookie cookie;
        try {
            cookie = PersistentHttpUserSessionCookie.getCookie(request);
        } catch (BadRequestException e) {
            throw new UnauthorizedException("Malformed cookie");
        }
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
        } catch (DatabaseException e) {
            throw new InternalServerException(e);
        }

        AuthenticationResult result = session.validate(cookie);
        if (result != AuthenticationResult.SUCCESS) {
            throw new UnauthorizedException("Validating " + cookie + " for " + session + " resulted in " + result);
        }

        return session;
    }

    /** authenticates, or null if no authentication */
    public static AuthenticatedUserSession authenticateHttpUserSession(Database database, FullHttpRequest request) throws UnauthorizedException, InternalServerException {
        HttpUserSession session = getHttpUserSession(database, request);
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

    /** create session and persistent session for user */
    private static AuthenticatedUserSession createHttpSession(HttpConfig httpConfig, Database database, User user, HttpHeaders httpHeaders) throws InternalServerException {
        try {
            // create session
            HttpUserSession httpUserSession = new HttpUserSession(httpConfig, user, httpHeaders);
            database.addHttpSession(httpUserSession);

            // create new persistent session
            PersistentHttpUserSession a = new PersistentHttpUserSession(httpConfig, httpUserSession, httpHeaders);
            database.addPersistentHttpUserSession(a);
        } catch (DatabaseException e) {
            throw new InternalServerException(e);
        }

        return new AuthenticatedUserSession(user);
    }

    /** logs in if not authenticated, or null if no auth */
    public static AuthenticatedUserSession logInHttpSession(HttpConfig config, Database database, FullHttpRequest request, HttpHeaders httpHeaders) throws UnauthorizedException, InternalServerException {
        AuthenticatedUserSession session = authenticateHttpUserSession(database, request);
        if (session != null) {
            return session;
        }

        PersistentHttpUserSession s = getPersistentHttpUserSession(database, request);
        if (s == null) {
            return null;
        }

        User user;
        try {
            user = database.getUser(s.getId());
        } catch (DatabaseException e) {
            throw new InternalServerException(e);
        }
        if (user == null) {
            throw new InternalServerException("User somehow does not exist " + s);
        }

        return createHttpSession(config, database, user, httpHeaders);
    }

    /** creates new user if not logged in or authenticated */
    public static AuthenticatedUserSession createHttpSession(FullHttpRequest request, Database database, HttpConfig config, HttpHeaders headers) throws UnauthorizedException, InternalServerException {
        AuthenticatedUserSession session = logInHttpSession(config, database, request, headers);
        if (session != null) {
            return session;
        }

        User user = new User();
        try {
            database.addUser(user);
        } catch (DatabaseException e) {
            throw new InternalServerException(e);
        }

        return createHttpSession(config, database, user, headers);
    }
}
