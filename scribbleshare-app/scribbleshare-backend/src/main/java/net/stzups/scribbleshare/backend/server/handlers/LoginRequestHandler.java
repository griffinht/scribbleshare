package net.stzups.scribbleshare.backend.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import net.stzups.netty.http.exception.HttpException;
import net.stzups.netty.http.exception.exceptions.InternalServerException;
import net.stzups.netty.http.handler.RequestHandler;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.backend.data.PersistentHttpUserSession;
import net.stzups.scribbleshare.backend.data.database.databases.PersistentHttpSessionDatabase;
import net.stzups.scribbleshare.data.database.databases.HttpSessionDatabase;
import net.stzups.scribbleshare.data.database.databases.LoginDatabase;
import net.stzups.scribbleshare.data.database.databases.UserDatabase;
import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpConfig;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSession;
import net.stzups.scribbleshare.data.objects.authentication.login.Login;

import java.nio.charset.StandardCharsets;

import static net.stzups.netty.http.HttpUtils.send;

public class LoginRequestHandler<T extends LoginDatabase & UserDatabase & HttpSessionDatabase & PersistentHttpSessionDatabase> extends RequestHandler {
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

    private enum LoginResponseResult {
        SUCCESS(0),
        FAILED(1)
        ;
        private final int id;

        LoginResponseResult(int id) {
            this.id = id;
        }

        public void serialize(ByteBuf byteBuf) {
            byteBuf.writeByte((byte) id);
        }
    }

    private static class LoginResponse {
        private final LoginResponseResult status;

        LoginResponse(LoginResponseResult result) {
            this.status = result;
        }

        public void serialize(ByteBuf byteBuf) {
            status.serialize(byteBuf);
        }
    }

    static final String LOGIN_PAGE = "/login"; // the login page, where login requests should come from
    private static final String LOGIN_PATH = "/login"; // where login requests should go

    private final HttpConfig config;
    private final T database;

    public LoginRequestHandler(HttpConfig config, T database) {
        super(config, LOGIN_PAGE, LOGIN_PATH);
        this.database = database;
        this.config = config;
    }

    @Override
    public void handleRequest(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponse response) throws HttpException {
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

            ByteBuf byteBuf = Unpooled.buffer();
            new LoginResponse(LoginResponseResult.FAILED).serialize(byteBuf);
            send(ctx, request, response, byteBuf);
            byteBuf.release();
            return;
        }

        assert login != null : "Verified logins should never be null";

        User user;
        try {
            user = database.getUser(login.getId());
        } catch (DatabaseException e) {
            throw new InternalServerException(e);
        }
        if (user == null) {
            throw new InternalServerException("No user for id " + login.getId());
        }

        HttpUserSession userSession = new HttpUserSession(config, user, response);
        try {
            database.addHttpSession(userSession);
        } catch (DatabaseException e) {
            throw new InternalServerException(e);
        }
        if (loginRequest.remember) {
            PersistentHttpUserSession persistentHttpUserSession = new PersistentHttpUserSession(config, userSession, response);
            try {
                database.addPersistentHttpUserSession(persistentHttpUserSession);
            } catch (DatabaseException e) {
                throw new InternalServerException(e);
            }
        }

        response.headers().set(headers);
        ByteBuf byteBuf = Unpooled.buffer();
        new LoginResponse(LoginResponseResult.SUCCESS).serialize(byteBuf);
        send(ctx, request, response, byteBuf);
        Scribbleshare.getLogger(ctx).info("Logged in as " + user);
    }

    static String readString(ByteBuf byteBuf) {
        return byteBuf.readCharSequence(byteBuf.readUnsignedByte(), StandardCharsets.UTF_8).toString();
    }
}