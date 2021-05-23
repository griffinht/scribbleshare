package net.stzups.scribbleshare.server.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedInput;
import net.stzups.scribbleshare.Scribbleshare;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class HttpUtils {
    private static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private static final String HTTP_DATE_GMT_TIMEZONE = "GMT";

    public static void send(ChannelHandlerContext ctx, FullHttpRequest request, ByteBuf responseContent) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/octet-stream");
        response.content().writeBytes(responseContent);

        send(ctx, request, response);
    }

    public static boolean isModifiedSince(FullHttpRequest request, Timestamp lastModified) throws Exception {
        String ifModifiedSince = request.headers().get(HttpHeaderNames.IF_MODIFIED_SINCE);
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);//todo

            //round lastModified to nearest second and compare
            return Instant.ofEpochSecond(lastModified.getTime() / 1000).isAfter(dateFormatter.parse(ifModifiedSince).toInstant());
        }

        return true;
    }

    public static void setDateAndLastModified(HttpHeaders headers, Timestamp lastModified) {//todo
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        Calendar time = new GregorianCalendar();
        headers.set(HttpHeaderNames.DATE, dateFormatter.format(time.getTime()));

        headers.set(HttpHeaderNames.LAST_MODIFIED, dateFormatter.format(lastModified));
    }

    /** sets keep alive headers and returns whether the connection is keep alive */
    public static boolean setKeepAlive(FullHttpRequest request, HttpResponse response) {
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        if (!keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        } else if (request.protocolVersion().equals(HttpVersion.HTTP_1_0)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        return keepAlive;
    }



    public static void send(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response) {
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        if (request == null) { // assume no keep alive
            ctx.writeAndFlush(response);
            return;
        }

        boolean keepAlive = setKeepAlive(request, response);

        ChannelFuture flushPromise = ctx.writeAndFlush(response);

        if (!keepAlive) {
            // Close the connection as soon as the response is sent.
            flushPromise.addListener(ChannelFutureListener.CLOSE);
        }
    }

    public static void send(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponse response, HttpChunkedInput httpChunkedInput) {
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpChunkedInput.length());

        boolean keepAlive = setKeepAlive(request, response);

        // Write the initial line and the header.
        ctx.write(response);

        // HttpChunkedInput will write the end marker (LastHttpContent) for us.
        ChannelFuture lastContentFuture = ctx.writeAndFlush(httpChunkedInput, ctx.newProgressivePromise());

        if (!keepAlive) {
            // Close the connection when the whole content is written out.
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    public static void sendRedirect(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers, String newUri) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.MOVED_PERMANENTLY, Unpooled.EMPTY_BUFFER);
        response.headers().set(headers);
        response.headers().set(HttpHeaderNames.LOCATION, newUri);

        send(ctx, request, response);
    }

    public static void sendRedirect(ChannelHandlerContext ctx, FullHttpRequest request, String newUri) {
        sendRedirect(ctx, request, EmptyHttpHeaders.INSTANCE, newUri);
    }

    public static void send(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.EMPTY_BUFFER);

        send(ctx, request, response);
    }

    /** sends if stale, otherwise sends not modified */
    public static void sendChunkedResource(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers, ChunkedInput<ByteBuf> chunkedInput, Timestamp lastModified) throws Exception {
        setDateAndLastModified(headers, lastModified);
        if (isModifiedSince(request, lastModified)) {
            Scribbleshare.getLogger(ctx).info("Uncached");
            sendChunkedResource(ctx, request, headers, chunkedInput);
        } else {
            Scribbleshare.getLogger(ctx).info("Cached");
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_MODIFIED, Unpooled.EMPTY_BUFFER);
            response.headers().set(headers);

            send(ctx, request, response);
        }
    }

    /** sends resource with headers */
    public static void sendChunkedResource(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers, ChunkedInput<ByteBuf> chunkedInput) {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(headers);

        send(ctx, request, response, new HttpChunkedInput(chunkedInput));
    }

    /** make sure the file being sent is valid */
    public static void sendFile(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers, File file, String mimeType) throws Exception {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            long fileLength = randomAccessFile.length();
            if (mimeType == null) {
                Scribbleshare.getLogger(ctx).warning("Unknown MIME type for file " + file.getName());
                send(ctx, request, HttpResponseStatus.NOT_FOUND);
                return;
            }
            headers.set(HttpHeaderNames.CONTENT_TYPE, mimeType);

            sendChunkedResource(ctx, request, headers, new ChunkedFile(randomAccessFile, 0, fileLength, 8192), Timestamp.from(Instant.ofEpochMilli(file.lastModified())));
        } catch (FileNotFoundException ignore) {
            send(ctx, request, HttpResponseStatus.NOT_FOUND);
        }
    }

    public static void setCookie(HttpHeaders headers, Cookie cookie) {
        headers.add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
    }







}
