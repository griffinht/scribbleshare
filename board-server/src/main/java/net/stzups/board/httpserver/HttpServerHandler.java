package net.stzups.board.httpserver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpServerHandler extends ChannelInboundHandlerAdapter {
    private HttpRequest httpRequest;

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx)
    {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest)
        {
            httpRequest = (HttpRequest) msg;

            if (HttpUtil.is100ContinueExpected(httpRequest)) {
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HTTP_1_1,
                        CONTINUE,
                        Unpooled.EMPTY_BUFFER);
                ctx.write(response);
            }
        }

        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;
            ByteBuf content = httpContent.content();
            if (content.isReadable()) {
                //todo
            }

            if (msg instanceof LastHttpContent) {
                LastHttpContent lastHttpContent = (LastHttpContent) msg;
                if (!lastHttpContent.trailingHeaders().isEmpty()) {
                    //todo?
                }

                if (!writeResponse(httpRequest, lastHttpContent, ctx, new StringBuilder("hello there"))) {
                    ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    {
        cause.printStackTrace();
        ctx.writeAndFlush(new DefaultFullHttpResponse( //remove this?
                HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                copiedBuffer(cause.getMessage().getBytes())
        ));
        ctx.close();
    }

    private boolean writeResponse(HttpRequest httpRequest, HttpObject httpObject, ChannelHandlerContext ctx, StringBuilder buffer) {
        boolean keepAlive = HttpUtil.isKeepAlive(httpRequest);

        FullHttpResponse response = new DefaultFullHttpResponse(
        HTTP_1_1, httpObject.decoderResult().isSuccess()? OK : BAD_REQUEST,
        Unpooled.copiedBuffer(buffer.toString(), CharsetUtil.UTF_8));

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

        if (keepAlive) {
            response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        ctx.write(response);

        return keepAlive;
    }
}
