package net.stzups.scribbleshare.backend.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.AttributeKey;
import net.stzups.scribbleshare.ScribbleshareConfig;
import net.stzups.scribbleshare.backend.ScribbleshareBackendConfig;
import net.stzups.scribbleshare.backend.server.http.HttpServerHandler;
import net.stzups.scribbleshare.data.database.ScribbleshareDatabase;
import net.stzups.scribbleshare.server.ServerInitializer;

import javax.net.ssl.SSLException;

@ChannelHandler.Sharable
public class BackendServerInitializer extends ServerInitializer {
    private static final AttributeKey<ScribbleshareDatabase> DATABASE = AttributeKey.valueOf(BackendServerInitializer.class, "DATABASE");
    public static ScribbleshareDatabase getDatabase(ChannelHandlerContext ctx) {
        return ctx.channel().attr(DATABASE).get();
    }

   private final HttpServerHandler httpServerHandler;

   private final ScribbleshareDatabase database;

    public BackendServerInitializer(ScribbleshareBackendConfig config, ScribbleshareDatabase database) throws SSLException {
        super(config);
        this.database = database;
        httpServerHandler = new HttpServerHandler(config);
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        super.initChannel(channel);

        channel.attr(DATABASE).set(database);

        channel.pipeline().addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(Integer.MAX_VALUE)) //2gb todo decrease
                .addLast(new HttpContentCompressor())
                .addLast(new ChunkedWriteHandler())
                .addLast(httpServerHandler);
    }
}