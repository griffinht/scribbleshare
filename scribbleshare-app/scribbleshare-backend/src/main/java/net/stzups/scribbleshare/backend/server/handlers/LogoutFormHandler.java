package net.stzups.scribbleshare.backend.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import net.stzups.scribbleshare.data.database.ScribbleshareDatabase;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpConfig;
import net.stzups.scribbleshare.server.http.HttpUtils;
import net.stzups.scribbleshare.server.http.exception.exceptions.BadRequestException;
import net.stzups.scribbleshare.server.http.exception.exceptions.InternalServerException;
import net.stzups.scribbleshare.server.http.handler.FormHandler;
import net.stzups.scribbleshare.server.http.objects.Form;

public class LogoutFormHandler extends FormHandler {
    private static final String LOGOUT_PAGE = "/"; // the logout page, where logout requests should come from
    private static final String LOGOUT_PATH = "/logout"; // where logout requests should go

    private final HttpConfig config;
    private final ScribbleshareDatabase database;

    public LogoutFormHandler(HttpConfig config, ScribbleshareDatabase database) {
        super(LOGOUT_PATH);
        this.config = config;
        this.database = database;
    }

    @Override
    public boolean handle(ChannelHandlerContext ctx, FullHttpRequest request, Form form) throws BadRequestException, InternalServerException {
/*                        Form form = new Form(request);//todo necessary?


                HttpHeaders headers = new DefaultHttpHeaders();
                HttpSessionCookie cookie = HttpUserSessionCookie.getHttpUserSessionCookie(request);
            if (cookie != null) {
                HttpUserSession httpUserSession = database.getHttpSession(cookie);
                if (httpUserSession != null) {
                    if (httpUserSession.validate(cookie)) {
                        httpUserSession.clearCookie(config, headers);
                        try {
                            database.expireHttpSession(httpUserSession);
                        } catch (FailedException e) {
                            //todo still send clear cookie
                            throw new InternalServerException("Failed to log out user", e);
                        }
                    } else {
                        Scribbleshare.getLogger(ctx).warning("Tried to log out of existing session with bad authentication");
                    }
                } else {
                    Scribbleshare.getLogger(ctx).warning("Tried to log out of non existent session");
                }
            }

            PersistentHttpUserSessionCookie persistentCookie = PersistentHttpUserSessionCookie.getHttpUserSessionCookie(request);
            if (persistentCookie != null) {
                PersistentHttpUserSession persistentHttpUserSession = database.getPersistentHttpUserSession(persistentCookie);
                if (persistentHttpUserSession != null) {
                    if (persistentHttpUserSession.validate(persistentCookie)) {
                        persistentCookie.clearCookie(config, headers);
                        try {
                            database.expirePersistentHttpUserSession(persistentHttpUserSession);
                        } catch (FailedException e) {
                            //todo still send clear cookie
                            throw new InternalServerException("Failed to log out user", e);
                        }
                    } else {
                        Scribbleshare.getLogger(ctx).warning("Tried to log out of existing persistent session with bad authentication");
                        //todo error
                    }
                } else {
                    Scribbleshare.getLogger(ctx).warning("Tried to log out of non existent persistent session");
                    //todo error
                }
            }
                sendRedirect(ctx, request, headers, LOGOUT_SUCCESS);
                return true;*/
        HttpUtils.send(ctx, request, HttpResponseStatus.NOT_FOUND);
        return true;
    }
}
