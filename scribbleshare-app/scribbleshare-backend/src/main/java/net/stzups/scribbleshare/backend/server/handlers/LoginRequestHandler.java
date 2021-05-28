package net.stzups.scribbleshare.backend.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.data.database.ScribbleshareDatabase;
import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpConfig;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSession;
import net.stzups.scribbleshare.data.objects.authentication.http.PersistentHttpUserSession;
import net.stzups.scribbleshare.data.objects.authentication.login.Login;
import net.stzups.scribbleshare.server.http.exception.HttpException;
import net.stzups.scribbleshare.server.http.exception.exceptions.InternalServerException;
import net.stzups.scribbleshare.server.http.handler.RequestHandler;
import net.stzups.scribbleshare.server.http.objects.Route;

import java.nio.charset.StandardCharsets;

import static net.stzups.scribbleshare.server.http.HttpUtils.send;

public class LoginRequestHandler extends RequestHandler {
    private static class LoginRequest {
        private final String username;
        private final String password;
        private final boolean remember;

        LoginRequest(ByteBuf byteBuf) {
            username = readString(byteBuf);
            password = readString(byteBuf);
            remember = byteBuf.readBoolean();
        }
    }

    static final String LOGIN_PAGE = "/"; // the login page, where login requests should come from
    private static final String LOGIN_PATH = PersistentHttpUserSession.LOGIN_PATH; // where login requests should go

    private final HttpConfig config;
    private final ScribbleshareDatabase database;

    public LoginRequestHandler(HttpConfig config, ScribbleshareDatabase database) {
        super(LOGIN_PATH);
        this.database = database;
        this.config = config;
    }

    @Override
    public boolean handle(ChannelHandlerContext ctx, FullHttpRequest request, Route route) throws HttpException {
        LoginRequest loginRequest = new LoginRequest(request.content());

        Login login;
        try {
            login = database.getLogin(loginRequest.username);
        } catch (DatabaseException e) {
            throw new InternalServerException(e);
        }
        if (!Login.verify(login, loginRequest.password.getBytes(StandardCharsets.UTF_8))) {
            //todo rate limit and generic error handling
            if (login == null) {
                Scribbleshare.getLogger(ctx).info("Failed login attempt with bad username " + loginRequest.username);
            } else {
                Scribbleshare.getLogger(ctx).info("Failed login attempt with bad password for username " + loginRequest.username);
            }

            send(ctx, request, HttpResponseStatus.UNAUTHORIZED);
            return true;
        }

        assert login != null : "Verified logins should never be null";

        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        User user;
        try {
            user = database.getUser(login.getId());
        } catch (DatabaseException e) {
            throw new InternalServerException(e);
        }
        if (user == null) {
            throw new InternalServerException("No user for id " + login.getId());
        }

        HttpUserSession userSession = new HttpUserSession(config, user, httpHeaders);
        try {
            database.addHttpSession(userSession);
        } catch (DatabaseException e) {
            throw new InternalServerException(e);
        }
        if (loginRequest.remember) {
            PersistentHttpUserSession persistentHttpUserSession = new PersistentHttpUserSession(config, userSession, httpHeaders);
            try {
                database.addPersistentHttpUserSession(persistentHttpUserSession);
            } catch (DatabaseException e) {
                throw new InternalServerException(e);
            }
        }

        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.EMPTY_BUFFER);
        response.headers().set(httpHeaders);
        send(ctx, request, response);
        return true;
    }

    static String readString(ByteBuf byteBuf) {
        return byteBuf.readCharSequence(byteBuf.readUnsignedByte(), StandardCharsets.UTF_8).toString();
    }
}