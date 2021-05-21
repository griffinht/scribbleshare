package net.stzups.scribbleshare.room.server.websocket.state.states;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.room.server.websocket.state.State;

public class InitialState extends State {
    private final User user;

    public InitialState(User user) {
        this.user = user;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) {
        if (event instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            Scribbleshare.getLogger(ctx).info("WebSocket connection initialized");
            setState(ctx, new HandshakeState(user));
            return;
        }

        if (event instanceof WebSocketServerProtocolHandler.ServerHandshakeStateEvent) return; //deprecated but still fired

        super.userEventTriggered(ctx, event);
    }
}
