package net.stzups.scribbleshare.room.server.websocket.state;

import io.netty.channel.ChannelHandlerContext;
import net.stzups.netty.http.exception.exceptions.InternalServerException;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.room.server.websocket.ClientMessageException;
import net.stzups.scribbleshare.room.server.websocket.ClientMessageHandler;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.scribbleshare.room.server.websocket.state.states.InitialState;

public abstract class State {
    public static void setState(ChannelHandlerContext ctx, State state) {
        ClientMessageHandler.getState(ctx).set(state);

        if (!(state instanceof InitialState)) {
            Scribbleshare.getLogger(ctx).info(state.toString());
        }
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
