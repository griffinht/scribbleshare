package net.stzups.scribbleshare.room.server.websocket.state.states;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.data.objects.authentication.AuthenticatedUserSession;
import net.stzups.scribbleshare.room.server.websocket.state.State;

public class InitialState extends State {
    private final AuthenticatedUserSession session;

    public InitialState(AuthenticatedUserSession session) {
        this.session = session;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) {
        if (event instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            Scribbleshare.getLogger(ctx).info("WebSocket connection initialized");
            setState(ctx, new HandshakeState(session));
            return;
        }

        if (event instanceof WebSocketServerProtocolHandler.ServerHandshakeStateEvent) return; //deprecated but still fired

        super.userEventTriggered(ctx, event);
    }
}
