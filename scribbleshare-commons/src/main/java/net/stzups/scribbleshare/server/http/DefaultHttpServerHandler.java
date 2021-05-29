package net.stzups.scribbleshare.server.http;

import net.stzups.scribbleshare.data.objects.authentication.http.HttpConfig;
import net.stzups.scribbleshare.server.http.handler.handlers.HealthcheckRequestHandler;
import net.stzups.scribbleshare.server.http.handler.handlers.LogHandler;
import net.stzups.scribbleshare.server.http.handler.handlers.OriginHandler;

public class DefaultHttpServerHandler extends HttpServerHandler {
    public DefaultHttpServerHandler(HttpConfig config) {
        addLast(new HealthcheckRequestHandler())
                .addLast(new LogHandler())
                .addLast(new OriginHandler(config, config.getOrigin()));
    }
}
