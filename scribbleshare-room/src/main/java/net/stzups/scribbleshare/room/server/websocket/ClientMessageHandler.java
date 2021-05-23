package net.stzups.scribbleshare.room.server.websocket;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.data.database.ScribbleshareDatabase;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.scribbleshare.room.server.websocket.state.State;
import net.stzups.scribbleshare.room.server.websocket.state.states.InitialState;
import net.stzups.scribbleshare.server.http.handlers.HttpAuthenticator;

import java.util.logging.Level;

@ChannelHandler.Sharable
public class ClientMessageHandler extends SimpleChannelInboundHandler<ClientMessage> {
    private static final AttributeKey<State> STATE = AttributeKey.valueOf(ClientMessageHandler.class, "STATE");

    public static Attribute<State> getState(ChannelHandlerContext ctx) {
        return ctx.channel().attr(STATE);
    }

    private final ScribbleshareDatabase database;

    public ClientMessageHandler(ScribbleshareDatabase database) {
        this.database = database;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        State.setState(ctx, new InitialState(database.getUser(HttpAuthenticator.getUser(ctx))));
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
        Scribbleshare.getLogger(ctx).log(Level.WARNING, "Unhandled exception while in connection state " + getState(ctx).get(), cause);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ClientMessage message) throws ClientMessageException {
        ctx.channel().attr(STATE).get().message(ctx, message);
    }
}
