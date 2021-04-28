package net.stzups.scribbleshare.room.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AttributeKey;
import net.stzups.scribbleshare.data.objects.session.HttpSession;
import net.stzups.scribbleshare.room.ScribbleshareRoom;

import java.util.List;

@ChannelHandler.Sharable
public class HttpAuthenticator extends MessageToMessageDecoder<FullHttpRequest> {
    public static AttributeKey<Long> USER = AttributeKey.valueOf(HttpAuthenticator.class, "USER");
    private static AttributeKey<Boolean> A = AttributeKey.valueOf(HttpAuthenticator.class, "A");

    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest request, List<Object> out) {
        if (ctx.channel().attr(A).get() != null) {
            out.add(request.retain());
            return;
        }
        ServerInitializer.getLogger(ctx).info(request.method() + " " + request.uri());

        if (request.decoderResult().isFailure()) {
            send(ctx, request, HttpResponseStatus.BAD_REQUEST);
            ServerInitializer.getLogger(ctx).info("Bad request");
            return;
        }

        if (!request.method().equals(HttpMethod.GET)) {
            send(ctx, request, HttpResponseStatus.METHOD_NOT_ALLOWED);
            ServerInitializer.getLogger(ctx).info("Bad method");
            return;
        }

        if (request.uri().equals("/healthcheck")) {
            send(ctx, request, HttpResponseStatus.OK);
            ServerInitializer.getLogger(ctx).info("Good healthcheck response");
            return;
        }

        if (!request.uri().equals(ServerInitializer.WEBSOCKET_PATH)) {
            send(ctx, request, HttpResponseStatus.NOT_FOUND);
            ServerInitializer.getLogger(ctx).info("Bad uri");
            return;
        }

        HttpSession.ClientCookie cookie = HttpSession.ClientCookie.getClientCookie(request, HttpSession.COOKIE_NAME);
        if (cookie != null) {
            HttpSession httpSession = ScribbleshareRoom.getDatabase().getHttpSession(cookie.getId());
            if (httpSession != null && httpSession.validate(cookie.getToken())) {
                ServerInitializer.getLogger(ctx).info("Authenticated with id " + httpSession.getUser());
                ctx.channel().attr(USER).set(httpSession.getUser());
                //now that we have an good authenticated HTTP request, set up WebSocket pipeline
                //ctx.pipeline().remove(this);
                //pass on this request
                ctx.channel().attr(A).set(true);
                out.add(request.retain());
            } else {
                ServerInitializer.getLogger(ctx).warning("Bad authentication");
                //bad authentication attempt
                //todo rate limit timeout server a proper response???
                send(ctx, request, HttpResponseStatus.UNAUTHORIZED);
            }
        } else {
            ServerInitializer.getLogger(ctx).info("No authentication");
            send(ctx, request, HttpResponseStatus.UNAUTHORIZED);
        }
    }

    private static void send(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.EMPTY_BUFFER);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        if (request == null) { // assume no keep alive
            ctx.writeAndFlush(response);
            return;
        }

        ChannelFuture flushPromise = ctx.writeAndFlush(response);
        flushPromise.addListener(ChannelFutureListener.CLOSE);
    }
}
