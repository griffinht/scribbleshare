package net.stzups.scribbleshare.server.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
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
import net.stzups.scribbleshare.server.http.exception.exceptions.BadRequestException;
import net.stzups.scribbleshare.server.http.exception.exceptions.NotFoundException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class HttpUtils {
    private static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private static final String HTTP_DATE_GMT_TIMEZONE = "GMT";





    /** sets keep alive headers and returns whether the connection is keep alive */
    public static boolean setKeepAlive(FullHttpRequest request, HttpResponse response) {
        boolean keepAlive;
        if (request != null) {
            keepAlive = HttpUtil.isKeepAlive(request);
        } else {
            keepAlive = false;
        }

        if (!keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        } else if (request.protocolVersion().equals(HttpVersion.HTTP_1_0)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        return keepAlive;
    }

    /**
     * Send {@link HttpResponse} with {@link ByteBuf} content
     */
    public static void send(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponse response, ByteBuf content) {
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        boolean keepAlive = setKeepAlive(request, response);

        ctx.write(response);
        ChannelFuture flushPromise = ctx.writeAndFlush(content);

        if (!keepAlive) {
            flushPromise.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * Send {@link HttpResponse} with no content
     */
    public static void send(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponse response) {
        send(ctx, request, response, Unpooled.EMPTY_BUFFER);
    }

    /**
     * Send {@link HttpResponse} followed by {@link HttpChunkedInput}
     */
    public static void send(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponse response, HttpChunkedInput httpChunkedInput) {
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpChunkedInput.length());

        boolean keepAlive = setKeepAlive(request, response);

        ctx.write(response);
        ChannelFuture lastContentFuture = ctx.writeAndFlush(httpChunkedInput, ctx.newProgressivePromise());

        if (!keepAlive) {
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * Send redirect to a new URI
     */
    public static void sendRedirect(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponse response, HttpResponseStatus status, String newUri) {
        response.setStatus(status);
        response.headers().set(HttpHeaderNames.LOCATION, newUri);

        send(ctx, request, response);
    }

    public static void setDateAndLastModified(HttpHeaders headers, Timestamp lastModified) {//todo
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        Calendar time = new GregorianCalendar();
        headers.set(HttpHeaderNames.DATE, dateFormatter.format(time.getTime()));

        headers.set(HttpHeaderNames.LAST_MODIFIED, dateFormatter.format(lastModified));
    }

    public static boolean isModifiedSince(FullHttpRequest request, Timestamp lastModified) throws BadRequestException {
        String ifModifiedSince = request.headers().get(HttpHeaderNames.IF_MODIFIED_SINCE);
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);//todo

            //round lastModified to nearest second and compare
            try {
                return Instant.ofEpochSecond(lastModified.getTime() / 1000).isAfter(dateFormatter.parse(ifModifiedSince).toInstant());
            } catch (ParseException e) {
                throw new BadRequestException("Exception while parsing date " + ifModifiedSince, e);
            }
        }

        return true;
    }

    /**
     * Send {@link ChunkedInput<ByteBuf>} if stale, or {@link HttpResponseStatus#NOT_MODIFIED} if fresh
     */
    public static void sendChunkedResource(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponse response, ChunkedInput<ByteBuf> chunkedInput, Timestamp lastModified) throws BadRequestException {
        setDateAndLastModified(response.headers(), lastModified);
        if (isModifiedSince(request, lastModified)) {
            //Scribbleshare.getLogger(ctx).info("Uncached");
            send(ctx, request, response, new HttpChunkedInput(chunkedInput));
        } else {
            //Scribbleshare.getLogger(ctx).info("Cached");
            response.setStatus(HttpResponseStatus.NOT_MODIFIED);

            send(ctx, request, response);
        }
    }

    /**
     * Send {@link File} with respect to caching
     */
    public static void sendFile(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponse response, File file, String mimeType) throws IOException, BadRequestException, NotFoundException {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            long fileLength = randomAccessFile.length();
            if (mimeType == null) {
                throw new NotFoundException("Unknown MIME type for file " + file.getName());
            }
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, mimeType);

            sendChunkedResource(ctx, request, response, new ChunkedFile(randomAccessFile, 0, fileLength, 8192), Timestamp.from(Instant.ofEpochMilli(file.lastModified())));
        } catch (FileNotFoundException ignore) {
            response.setStatus(HttpResponseStatus.NOT_FOUND);
            send(ctx, request, response);
        }
    }

    public static void setCookie(HttpHeaders headers, Cookie cookie) {
        headers.add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
    }
}
