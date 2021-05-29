package net.stzups.scribbleshare.server.http.handler.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpConfig;
import net.stzups.scribbleshare.server.http.HttpUtils;
import net.stzups.scribbleshare.server.http.exception.HttpException;
import net.stzups.scribbleshare.server.http.handler.HttpHandler;

public class RefererHandler extends HttpHandler {
    private final HttpConfig config;
    private final String origin;

    public RefererHandler(HttpConfig config) {
        this(config, null);
    }

    public RefererHandler(HttpConfig config, String origin) {
        super("/");
        this.config = config;
        this.origin = origin;
    }

    @Override
    public boolean handle(ChannelHandlerContext ctx, FullHttpRequest request) throws HttpException {
        String referer = request.headers().get(HttpHeaderNames.REFERER);
        if (referer != null) {
            String string = (config.getSSL() ? "https://" : "http://") + config.getDomain();
            if (!(this.origin != null && referer.startsWith(this.origin)) && !referer.startsWith(string) && !referer.startsWith(string + ":" + config.getPort())) {
                if (this.origin != null) { // null for a warning
                    Scribbleshare.getLogger(ctx).warning("Unknown referer " + referer + ", serving 404");
                    HttpUtils.send(ctx, request, HttpResponseStatus.NOT_FOUND);
                    return true;
                } else {
                    Scribbleshare.getLogger(ctx).warning("Unknown referer " + referer + ", request will still be handled normally");
                }
            }
        }

        return false;
    }
}
