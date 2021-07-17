package net.stzups.scribbleshare.backend.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import net.stzups.netty.http.exception.HttpException;
import net.stzups.netty.http.handler.HttpHandler;
import net.stzups.scribbleshare.backend.data.PersistentHttpUserSession;
import net.stzups.scribbleshare.backend.data.database.databases.PersistentHttpSessionDatabase;
import net.stzups.scribbleshare.data.database.databases.HttpSessionDatabase;
import net.stzups.scribbleshare.data.database.databases.UserDatabase;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpConfig;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSession;

public class AutoHandler<T extends HttpSessionDatabase & PersistentHttpSessionDatabase & UserDatabase> extends HttpHandler {
    private final HttpConfig config;
    private final T database;

    public AutoHandler(HttpConfig config, T database) {
        super("/");
        this.config = config;
        this.database = database;
    }

    @Override
    public boolean handle(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponse response) throws HttpException {
        HttpUserSession session = HttpUserSession.getSession(database, request);
        if (session != null) {
            return false;
        }

        HttpUserSession a = PersistentHttpUserSession.logIn(config, database, request, response);
        return false;
    }
}
