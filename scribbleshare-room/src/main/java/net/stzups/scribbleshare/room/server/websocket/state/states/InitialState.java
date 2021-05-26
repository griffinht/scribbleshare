package net.stzups.scribbleshare.room.server.websocket.state.states;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.room.server.websocket.state.State;
import net.stzups.scribbleshare.server.http.handlers.HttpAuthenticator;
import net.stzups.scribbleshare.util.DebugString;

public class InitialState extends State {

    public InitialState() {

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) {
        if (event instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            Scribbleshare.getLogger(ctx).info("WebSocket connection initialized");
            setState(ctx, new HandshakeState(HttpAuthenticator.getUser(ctx)));
            return;
        }

        if (event instanceof WebSocketServerProtocolHandler.ServerHandshakeStateEvent) return; //deprecated but still fired

        super.userEventTriggered(ctx, event);
    }

    @Override
    public String toString() {
        return DebugString.get(this)
                .toString();
    }
}
