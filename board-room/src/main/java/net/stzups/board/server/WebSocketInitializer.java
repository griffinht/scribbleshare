package net.stzups.board.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.AttributeKey;
import net.stzups.board.data.objects.HttpSession;
import net.stzups.board.server.websocket.PacketHandler;

import java.net.InetSocketAddress;

@ChannelHandler.Sharable
public class WebSocketInitializer extends ChannelInboundHandlerAdapter {
    //public static final AttributeKey<HttpSession> HTTP_SESSION_KEY = AttributeKey.valueOf(WebSocketInitializer.class, "HTTP_SESSION");

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) {
        if (event instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            WebSocketServerProtocolHandler.HandshakeComplete handshakeComplete = (WebSocketServerProtocolHandler.HandshakeComplete) event;
            //ctx.channel().attr(HTTP_SESSION_KEY).set(HttpSession.getSession(handshakeComplete.requestHeaders(), ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress()));
            ctx.pipeline().addLast(new PacketHandler());//todo give this a different executor https://stackoverflow.com/questions/49133447/how-can-you-safely-perform-blocking-operations-in-a-netty-channel-handler
        }
    }
}
