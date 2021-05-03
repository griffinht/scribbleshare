package net.stzups.scribbleshare.room.server.websocket;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.room.exceptions.ClientMessageException;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;

import java.util.logging.Level;

@ChannelHandler.Sharable
public class ClientMessageHandler extends SimpleChannelInboundHandler<ClientMessage> {
    private static final AttributeKey<State> STATE = AttributeKey.valueOf(ClientMessageHandler.class, "STATE");
    private static final AttributeKey<Client> CLIENT = AttributeKey.valueOf(ClientMessageHandler.class, "CLIENT");
    private static final AttributeKey<Room> ROOM = AttributeKey.valueOf(ClientMessageHandler.class, "ROOM");

    public static Attribute<State> getState(ChannelHandlerContext ctx) {
        return ctx.channel().attr(STATE);
    }

    public static Attribute<Client> getClient(ChannelHandlerContext ctx) {
        return ctx.channel().attr(CLIENT);
    }

    public static Attribute<Room> getRoom(ChannelHandlerContext ctx) {
        return ctx.channel().attr(ROOM);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        State.INITIAL.setState(ctx);
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
