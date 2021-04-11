package net.stzups.board.room.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AttributeKey;
import net.stzups.board.data.objects.session.HttpSession;
import net.stzups.board.room.BoardRoom;

import java.util.List;
import java.util.logging.Logger;


public class HttpAuthenticator extends MessageToMessageDecoder<FullHttpRequest> {
    public static AttributeKey<Long> USER = AttributeKey.valueOf(HttpAuthenticator.class, "USER");

    private Logger logger;

    HttpAuthenticator(Logger logger) {
        this.logger = logger;
    }


    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {

    }

    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest request, List<Object> out) throws Exception {
        HttpSession.ClientCookie cookie = HttpSession.ClientCookie.getClientCookie(request, HttpSession.COOKIE_NAME);
        if (cookie != null) {
            HttpSession httpSession = BoardRoom.getDatabase().getHttpSession(cookie.getId());
            if (httpSession != null && httpSession.validate(cookie.getToken())) {
                logger.info("Authenticated with id " + httpSession.getUser());
                ctx.channel().attr(USER).set(httpSession.getUser());
                //now that we have an good authenticated HTTP request, set up WebSocket pipeline
                ctx.pipeline().remove(this);
                //pass on this request
                out.add(request.retain());
            } else {
                logger.info("Bad authentication");
                //bad authentication attempt
                FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED);
                //todo rate limit timeout server a proper response???
                ctx.writeAndFlush(fullHttpResponse);
                ctx.close();
            }
        } else {
            logger.info("No authentication");
            FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED);
            ctx.writeAndFlush(fullHttpResponse);
            ctx.close();
        }
    }
}
