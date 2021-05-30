package net.stzups.scribbleshare.backend.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
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

    private final HttpConfig config;

    public LogoutRequestHandler(HttpConfig config) {
        super(config, LOGOUT_PAGE, LOGOUT_PATH);
        this.config = config;
    }

    @Override
    protected void handleRequest(ChannelHandlerContext ctx, FullHttpRequest request) throws HttpException { //todo validate
        HttpHeaders headers = new DefaultHttpHeaders();
        HttpUserSessionCookie.clearCookie(config, headers);
        PersistentHttpUserSessionCookie.clearCookie(config, headers);
        sendRedirect(ctx, request, HttpResponseStatus.SEE_OTHER, LOGOUT_SUCCESS, headers);
        Scribbleshare.getLogger(ctx).info("Logged out");
    }
}
