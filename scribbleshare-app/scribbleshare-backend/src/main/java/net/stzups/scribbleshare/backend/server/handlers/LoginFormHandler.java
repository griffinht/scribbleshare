package net.stzups.scribbleshare.backend.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.data.database.ScribbleshareDatabase;
import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpConfig;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSession;
import net.stzups.scribbleshare.data.objects.authentication.http.PersistentHttpUserSession;
import net.stzups.scribbleshare.data.objects.authentication.login.Login;
import net.stzups.scribbleshare.server.http.exception.exceptions.BadRequestException;
import net.stzups.scribbleshare.server.http.exception.exceptions.InternalServerException;
import net.stzups.scribbleshare.server.http.handler.FormHandler;
import net.stzups.scribbleshare.server.http.objects.Form;

import java.nio.charset.StandardCharsets;

import static net.stzups.scribbleshare.server.http.HttpUtils.sendRedirect;

public class LoginFormHandler extends FormHandler {
    static final String LOGIN_PAGE = "/login"; // the login page, where login requests should come from
    private static final String LOGIN_PATH = PersistentHttpUserSession.LOGIN_PATH; // where login requests should go
    private static final String LOGIN_SUCCESS = "/"; // redirect for a good login, should be the main page

    private final HttpConfig config;
    private final ScribbleshareDatabase database;

    public LoginFormHandler(HttpConfig config, ScribbleshareDatabase database) {
        super(LOGIN_PATH);
        this.database = database;
        this.config = config;
    }

    @Override
    public boolean handle(ChannelHandlerContext ctx, FullHttpRequest request, Form form) throws BadRequestException, InternalServerException {
        String username = form.getText("username");
        String password = form.getText("password");
        boolean remember = form.getCheckbox("remember");

        Login login;
        try {
            login = database.getLogin(username);
        } catch (DatabaseException e) {
            throw new InternalServerException(e);
        }
        if (!Login.verify(login, password.getBytes(StandardCharsets.UTF_8))) {
            //todo rate limit and generic error handling
            if (login == null) {
                Scribbleshare.getLogger(ctx).info("Failed login attempt with bad username " + username);
            } else {
                Scribbleshare.getLogger(ctx).info("Failed login attempt with bad password for username " + username);
            }

            sendRedirect(ctx, request, LOGIN_PAGE);
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
        if (remember) {
            PersistentHttpUserSession persistentHttpUserSession = new PersistentHttpUserSession(config, userSession, httpHeaders);
            try {
                database.addPersistentHttpUserSession(persistentHttpUserSession);
            } catch (DatabaseException e) {
                throw new InternalServerException(e);
            }
        }

        sendRedirect(ctx, request, httpHeaders, LOGIN_SUCCESS);
        return true;
    }
}
