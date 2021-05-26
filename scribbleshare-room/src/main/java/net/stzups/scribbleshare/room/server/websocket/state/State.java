package net.stzups.scribbleshare.room.server.websocket.state;

import io.netty.channel.ChannelHandlerContext;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.room.server.websocket.ClientMessageException;
import net.stzups.scribbleshare.room.server.websocket.ClientMessageHandler;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.scribbleshare.server.http.exception.exceptions.InternalServerException;

public abstract class State {
    public static void setState(ChannelHandlerContext ctx, State state) {
        Scribbleshare.getLogger(ctx).info(state.toString());
        ClientMessageHandler.getState(ctx).set(state);
    }

    public void channelInactive(ChannelHandlerContext ctx) {
        //todo throw new UnsupportedOperationException("Unhandled channel close");
    }

    public void userEventTriggered(ChannelHandlerContext ctx, Object event) {
        throw new UnsupportedOperationException("Unhandled Netty userEventTriggered " + event);
    }

    public void message(ChannelHandlerContext ctx, ClientMessage clientMessage) throws ClientMessageException, InternalServerException {
        throw new UnsupportedOperationException("Unhandled ClientMessage " + clientMessage.getClass().getSimpleName());
    }
}
