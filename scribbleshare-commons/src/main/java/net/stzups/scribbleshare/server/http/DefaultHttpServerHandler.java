package net.stzups.scribbleshare.server.http;

import net.stzups.scribbleshare.server.http.handler.handlers.HealthcheckRequestHandler;
import net.stzups.scribbleshare.server.http.handler.handlers.LogHandler;

public class DefaultHttpServerHandler extends HttpServerHandler {
    public DefaultHttpServerHandler() {
        addLast(new HealthcheckRequestHandler())
                .addLast(new LogHandler());
    }
}
