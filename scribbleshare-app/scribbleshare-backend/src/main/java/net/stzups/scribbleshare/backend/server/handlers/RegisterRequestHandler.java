package net.stzups.scribbleshare.backend.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.data.database.databases.HttpSessionDatabase;
import net.stzups.scribbleshare.data.database.databases.LoginDatabase;
import net.stzups.scribbleshare.data.database.databases.UserDatabase;
import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpConfig;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSession;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSessionCookie;
import net.stzups.scribbleshare.data.objects.authentication.login.Login;
import net.stzups.scribbleshare.server.http.exception.HttpException;
import net.stzups.scribbleshare.server.http.exception.exceptions.InternalServerException;
import net.stzups.scribbleshare.server.http.handler.RequestHandler;

import static net.stzups.scribbleshare.backend.server.handlers.LoginRequestHandler.readString;
import static net.stzups.scribbleshare.server.http.HttpUtils.send;
import static net.stzups.scribbleshare.server.http.HttpUtils.sendRedirect;

public class RegisterRequestHandler<T extends HttpSessionDatabase & UserDatabase & LoginDatabase> extends RequestHandler {
    private static class RegisterRequest {
        private final String username;
        private final byte[] password;

        RegisterRequest(ByteBuf byteBuf) {
            username = readString(byteBuf);
            password = new byte[byteBuf.readUnsignedByte()];
            byteBuf.readBytes(password);
        }
    }

    private static final String REGISTER_PAGE = "/register"; // the register page, where register requests should come from
    private static final String REGISTER_PATH = "/register"; // where register requests should go
    private static final String REGISTER_SUCCESS = LoginRequestHandler.LOGIN_PAGE; // redirect for a good register, should be the login page

    private final T database;

    public RegisterRequestHandler(HttpConfig config, T database) {
        super(config, REGISTER_PAGE, REGISTER_PATH);
        this.database = database;
    }

    @Override
    public void handleRequest(ChannelHandlerContext ctx, FullHttpRequest request) throws HttpException {

        // validate
        RegisterRequest registerRequest = new RegisterRequest(request.content());

        //todo validate
        if (false) {
            //todo rate limit and generic error handling

            sendRedirect(ctx, request, REGISTER_PAGE);
            return;
        }

        User user;
        HttpUserSessionCookie cookie = HttpUserSessionCookie.getCookie(request);
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
                    user = new User(registerRequest.username);
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
                user = new User(registerRequest.username);
                try {
                    database.addUser(user);
                } catch (DatabaseException e) {
                    throw new InternalServerException(e);
                }
            }
        } else {
            user = new User(registerRequest.username);
            try {
                database.addUser(user);
            } catch (DatabaseException e) {
                throw new InternalServerException(e);
            }
        }

        assert !user.isRegistered();

        Login login = new Login(user, registerRequest.password);
        boolean loginAdded;
        try {
            loginAdded = database.addLogin(login);
        } catch (DatabaseException e) {
            throw new InternalServerException(e);
        }
        if (!loginAdded) {
            Scribbleshare.getLogger(ctx).info("Tried to register with duplicate username " + registerRequest.username);
            send(ctx, request, HttpResponseStatus.CONFLICT);
            return;
        }

        Scribbleshare.getLogger(ctx).info("Registered with username " + registerRequest.username);

        sendRedirect(ctx, request, REGISTER_SUCCESS);
        return;
    }
}
