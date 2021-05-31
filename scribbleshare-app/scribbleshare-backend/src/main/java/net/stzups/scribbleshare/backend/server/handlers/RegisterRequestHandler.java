package net.stzups.scribbleshare.backend.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.data.database.databases.HttpSessionDatabase;
import net.stzups.scribbleshare.data.database.databases.LoginDatabase;
import net.stzups.scribbleshare.data.database.databases.UserDatabase;
import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpConfig;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSession;
import net.stzups.scribbleshare.data.objects.authentication.login.Login;
import net.stzups.scribbleshare.server.http.exception.HttpException;
import net.stzups.scribbleshare.server.http.exception.exceptions.BadRequestException;
import net.stzups.scribbleshare.server.http.exception.exceptions.InternalServerException;
import net.stzups.scribbleshare.server.http.handler.RequestHandler;
import net.stzups.scribbleshare.server.http.handler.handlers.HttpAuthenticator;

import static net.stzups.scribbleshare.backend.server.handlers.LoginRequestHandler.readString;
import static net.stzups.scribbleshare.server.http.HttpUtils.send;

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

    private enum RegisterResponseResult {
        SUCCESS(0),
        USERNAME_TAKEN(1)
        ;

        private final int id;

        private RegisterResponseResult(int id) {
            this.id = id;
        }

        public void serialize(ByteBuf byteBuf) {
            byteBuf.writeByte((byte) id);
        }
    }
    private static class RegisterResponse {
        private final RegisterResponseResult result;

        private RegisterResponse(RegisterResponseResult result) {
            this.result = result;
        }

        public void serialize(ByteBuf byteBuf) {
            result.serialize(byteBuf);
        }
    }

    static final String REGISTER_PAGE = "/register"; // the register page, where register requests should come from
    private static final String REGISTER_PATH = "/register"; // where register requests should go
    private static final String REGISTER_SUCCESS = LoginRequestHandler.LOGIN_PAGE; // redirect for a good register, should be the login page

    private final T database;

    public RegisterRequestHandler(HttpConfig config, T database) {
        super(config, REGISTER_PAGE, REGISTER_PATH);
        this.database = database;
    }

    @Override
    public void handleRequest(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponse response) throws HttpException {

        // validate
        RegisterRequest registerRequest = new RegisterRequest(request.content());

        //todo validate
        if (false) {

            throw new BadRequestException("Invalid todo");
        }

        User user;
        HttpUserSession session = HttpAuthenticator.getHttpUserSession(database, request);
        if (session != null) {
            User u;
            try {
                u = database.getUser(session.getUser());
            } catch (DatabaseException e) {
                throw new InternalServerException(e);
            }
            if (u == null) {
                throw new InternalServerException("User somehow does not exist for " + session);
            }
            if (u.isRegistered()) {
                Scribbleshare.getLogger(ctx).info("Registered user is creating a new account");
                user = new User(registerRequest.username);
                try {
                    database.addUser(user);
                } catch (DatabaseException e) {
                    throw new InternalServerException(e);
                }
            } else {
                Scribbleshare.getLogger(ctx).info("Temporary user is registering");
                user = u;
            }
        } else {
            Scribbleshare.getLogger(ctx).info("Brand new user is registering");
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
        RegisterResponseResult result;
        if (!loginAdded) {
            //todo hard rate limit
            Scribbleshare.getLogger(ctx).info("Tried to register with duplicate username " + registerRequest.username);

            result = RegisterResponseResult.USERNAME_TAKEN;//todo delete old user????? because it was already added above and now its dead
        } else {
            result = RegisterResponseResult.SUCCESS;
        }

        if (result == RegisterResponseResult.SUCCESS) {
            Scribbleshare.getLogger(ctx).info("Registered with username " + registerRequest.username + " for user " + user);
        }

        ByteBuf byteBuf = Unpooled.buffer();
        new RegisterResponse(result).serialize(byteBuf);
        send(ctx, request, response, byteBuf);
    }
}
