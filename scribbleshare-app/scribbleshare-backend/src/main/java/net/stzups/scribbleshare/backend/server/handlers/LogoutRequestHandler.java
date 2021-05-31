package net.stzups.scribbleshare.backend.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.backend.data.PersistentHttpUserSessionCookie;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpConfig;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSessionCookie;
import net.stzups.scribbleshare.server.http.exception.HttpException;
import net.stzups.scribbleshare.server.http.handler.RequestHandler;

import static net.stzups.scribbleshare.server.http.HttpUtils.sendRedirect;

public class LogoutRequestHandler extends RequestHandler {
    private static final String LOGOUT_PAGE = "/logout"; // the logout page, where logout requests should come from
    private static final String LOGOUT_PATH = "/logout"; // where logout requests should go
    private static final String LOGOUT_SUCCESS = LoginRequestHandler.LOGIN_PAGE;

    public LogoutRequestHandler(HttpConfig config) {
        super(config, LOGOUT_PAGE, LOGOUT_PATH);
    }

    @Override
    protected void handleRequest(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponse response) throws HttpException { //todo validate
        HttpUserSessionCookie.clearCookie(response);
        PersistentHttpUserSessionCookie.clearCookie(response);
        sendRedirect(ctx, request, response, HttpResponseStatus.SEE_OTHER, LOGOUT_SUCCESS);
        Scribbleshare.getLogger(ctx).info("Logged out");
    }
}
