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
import net.stzups.scribbleshare.data.objects.authentication.http.HttpSessionCookie;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSession;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSessionCookie;
import net.stzups.scribbleshare.data.objects.authentication.http.PersistentHttpUserSession;
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

        AuthenticatedUserSession session = authenticateHttpUserSession(request, database);
        if (session == null) {
            throw new UnauthorizedException("No authentication");
        }

        Scribbleshare.getLogger(ctx).info("" + session);
        ctx.channel().attr(USER).set(session);
        return true;
    }

    /** authenticates, or null if no authentication */
    public static AuthenticatedUserSession authenticateHttpUserSession(FullHttpRequest request, Database database) throws UnauthorizedException, InternalServerException {
        HttpSessionCookie sessionCookie;
        try {
            sessionCookie = HttpUserSessionCookie.getHttpUserSessionCookie(request);
        } catch (BadRequestException e) {
            throw new UnauthorizedException("Malformed cookie", e);
        }
        if (sessionCookie == null) {
            return null;
        }

        User user;
        try {
            HttpUserSession userSession = database.getHttpSession(sessionCookie);
            if (userSession == null) {
                throw new UnauthorizedException("Bad authentication (bad id)");
            }

            AuthenticationResult result = userSession.validate(sessionCookie);
            if (result != AuthenticationResult.SUCCESS) {
                throw new UnauthorizedException("Bad authentication " + result);
            }

            user = database.getUser(userSession.getUser());
            if (user == null) {
                throw new InternalServerException("Unknown user for authentication");
            }
        } catch (DatabaseException e) {
            throw new InternalServerException(e);
        }

        return new AuthenticatedUserSession(user);
    }

    /** create session and persistent session for user */
    private static AuthenticatedUserSession createHttpSession(User user, HttpConfig httpConfig, Database database, HttpHeaders httpHeaders) throws InternalServerException {
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
    public static AuthenticatedUserSession logInHttpSession(FullHttpRequest request, Database database, HttpHeaders httpHeaders, HttpConfig httpConfig) throws UnauthorizedException, InternalServerException {
        AuthenticatedUserSession session = authenticateHttpUserSession(request, database);
        if (session != null) {
            return session;
        }

        HttpSessionCookie sessionCookie;
        try {
            sessionCookie = HttpUserSessionCookie.getHttpUserSessionCookie(request);
        } catch (BadRequestException e) {
            throw new UnauthorizedException(e);
        }
        if (sessionCookie == null) {
            return null;
        }

        User user;
        try {
            PersistentHttpUserSession persistentHttpUserSession = database.getPersistentHttpUserSession(sessionCookie);
            if (persistentHttpUserSession == null) {
                throw new UnauthorizedException("Bad authentication (bad id)");
            }

            database.expirePersistentHttpUserSession(persistentHttpUserSession);

            AuthenticationResult result = persistentHttpUserSession.validate(sessionCookie);
            if (result != AuthenticationResult.SUCCESS) {
                throw new UnauthorizedException("Bad authentication " + result);
            }
            // now logged in

            user = database.getUser(persistentHttpUserSession.getId());
            if (user == null) {
                throw new InternalServerException("User somehow does not exist " + persistentHttpUserSession.getUser());
            }

        } catch (DatabaseException e) {
            throw new InternalServerException(e);
        }

        return createHttpSession(user, httpConfig, database, httpHeaders);
    }

    /** creates new user if not logged in or authenticated */
    public static AuthenticatedUserSession createHttpSession(FullHttpRequest request, Database database, HttpConfig httpConfig, HttpHeaders httpHeaders) throws UnauthorizedException, InternalServerException {
        AuthenticatedUserSession session = logInHttpSession(request, database, httpHeaders, httpConfig);
        if (session != null) {
            return session;
        }

        User user = new User();
        try {
            database.addUser(user);
        } catch (DatabaseException e) {
            throw new InternalServerException(e);
        }

        return createHttpSession(user, httpConfig, database, httpHeaders);
    }
}