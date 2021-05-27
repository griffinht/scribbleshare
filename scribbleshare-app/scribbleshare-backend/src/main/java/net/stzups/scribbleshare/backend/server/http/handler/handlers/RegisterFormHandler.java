package net.stzups.scribbleshare.backend.server.http.handler.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.backend.server.http.handler.FormHandler;
import net.stzups.scribbleshare.data.database.ScribbleshareDatabase;
import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpConfig;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpSessionCookie;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSession;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSessionCookie;
import net.stzups.scribbleshare.data.objects.authentication.login.Login;
import net.stzups.scribbleshare.server.http.exception.exceptions.BadRequestException;
import net.stzups.scribbleshare.server.http.exception.exceptions.InternalServerException;
import net.stzups.scribbleshare.server.http.objects.Form;

import java.nio.charset.StandardCharsets;

import static net.stzups.scribbleshare.server.http.HttpUtils.sendRedirect;

public class RegisterFormHandler extends FormHandler {
    private static final String REGISTER_PAGE = "/register"; // the register page, where register requests should come from
    private static final String REGISTER_PATH = "/register"; // where register requests should go
    private static final String REGISTER_SUCCESS = LoginFormHandler.LOGIN_PAGE; // redirect for a good register, should be the login page

    private final HttpConfig config;
    private final ScribbleshareDatabase database;

    public RegisterFormHandler(HttpConfig config, ScribbleshareDatabase database) {
        super(REGISTER_PATH);
        this.config = config;
        this.database = database;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest request, Form form) throws BadRequestException, InternalServerException {

        // validate

        String username = form.getText("username");
        String password = form.getText("password");

        //todo validate
        if (false) {
            //todo rate limit and generic error handling

            sendRedirect(ctx, request, REGISTER_PAGE);
            return;
        }

        User user;
        HttpSessionCookie cookie = HttpUserSessionCookie.getHttpUserSessionCookie(request);
        if (cookie != null) {
            HttpUserSession httpSession;
            try {
                httpSession = database.getHttpSession(cookie);
            } catch (DatabaseException e) {
                throw new InternalServerException(e);
            }
            if (httpSession != null) {
                User u;
                try {
                    u = database.getUser(httpSession.getUser());
                } catch (DatabaseException e) {
                    throw new InternalServerException(e);
                }
                if (u == null) {
                    throw new InternalServerException("User somehow does not exist for " + httpSession);
                }
                if (u.isRegistered()) {
                    Scribbleshare.getLogger(ctx).info("Registered user is creating a new account");
                    user = new User(username);
                    try {
                        database.addUser(user);
                    } catch (DatabaseException e) {
                        throw new InternalServerException("todo", e);
                    }
                } else {
                    Scribbleshare.getLogger(ctx).info("Temporary user is registering");
                    user = u;
                }
            } else {
                user = new User(username);
                try {
                    database.addUser(user);
                } catch (DatabaseException e) {
                    throw new InternalServerException(e);
                }
            }
        } else {
            user = new User(username);
            try {
                database.addUser(user);
            } catch (DatabaseException e) {
                throw new InternalServerException(e);
            }
        }

        assert !user.isRegistered();

        Login login = new Login(user, password.getBytes(StandardCharsets.UTF_8));
        boolean loginAdded;
        try {
            loginAdded = database.addLogin(login);
        } catch (DatabaseException e) {
            throw new InternalServerException(e);
        }
        if (!loginAdded) {
            Scribbleshare.getLogger(ctx).info("Tried to register with duplicate username " + username);
            sendRedirect(ctx, request, REGISTER_PAGE);
            return;
        }

        Scribbleshare.getLogger(ctx).info("Registered with username " + username);

        sendRedirect(ctx, request, REGISTER_SUCCESS);
        return;
    }
}
