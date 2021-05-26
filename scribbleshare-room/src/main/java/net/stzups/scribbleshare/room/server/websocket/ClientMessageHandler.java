package net.stzups.scribbleshare.room.server.websocket;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.scribbleshare.room.server.websocket.state.State;
import net.stzups.scribbleshare.room.server.websocket.state.states.InitialState;
import net.stzups.scribbleshare.server.http.exception.exceptions.InternalServerException;
import net.stzups.scribbleshare.server.http.handlers.HttpAuthenticator;

import java.util.logging.Level;

@ChannelHandler.Sharable
public class ClientMessageHandler extends SimpleChannelInboundHandler<ClientMessage> {
    private static final AttributeKey<State> STATE = AttributeKey.valueOf(ClientMessageHandler.class, "STATE");

    public static Attribute<State> getState(ChannelHandlerContext ctx) {
        return ctx.channel().attr(STATE);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        State.setState(ctx, new InitialState(HttpAuthenticator.getUser(ctx)));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctx.channel().attr(STATE).get().channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) {
        ctx.channel().attr(STATE).get().userEventTriggered(ctx, event);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Scribbleshare.getLogger(ctx).log(Level.WARNING, "Unhandled exception while in " + getState(ctx).get(), cause);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ClientMessage message) {
        try {
            ctx.channel().attr(STATE).get().message(ctx, message);
        } catch (ClientMessageException | InternalServerException e) {
            Scribbleshare.getLogger(ctx).log(Level.WARNING, "Exception while handling message while in " + getState(ctx), e);
            //todo write exception
            //todo toString for state
        }
    }
}
