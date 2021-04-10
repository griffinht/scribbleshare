package net.stzups.board.room.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.AttributeKey;
import net.stzups.board.data.objects.session.HttpSession;
import net.stzups.board.room.BoardRoom;

import java.util.logging.Logger;


public class HttpAuthenticator extends SimpleChannelInboundHandler<FullHttpRequest> {
    public static AttributeKey<Long> USER = AttributeKey.valueOf(HttpAuthenticator.class, "USER");

    private Logger logger;

    HttpAuthenticator(Logger logger) {
        this.logger = logger;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        HttpSession.ClientCookie cookie = HttpSession.ClientCookie.getClientCookie(request, HttpSession.COOKIE_NAME);
        if (cookie != null) {
            HttpSession httpSession = BoardRoom.getDatabase().getHttpSession(cookie.getId());
            if (httpSession != null && httpSession.validate(cookie.getToken())) {
                logger.info("Authenticated with id " + httpSession.getUser());
                ctx.channel().attr(USER).set(httpSession.getUser());
            } else {
                logger.info("Bad authentication");
                //bad authentication attempt todo rate limit timeout server a proper response???
                ctx.close();
            }
        } else {
            logger.info("No authentication");
            ctx.close();//must be authenticated
        }
    }
}
