package net.stzups.board.backend.server.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;
import net.stzups.board.backend.BoardBackend;
import net.stzups.board.backend.BoardBackendConfigKeys;
import net.stzups.board.data.objects.User;
import net.stzups.board.data.objects.session.HttpSession;
import net.stzups.board.data.objects.session.PersistentHttpSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final File HTTP_ROOT = new File(BoardBackend.getConfig().getString(BoardBackendConfigKeys.HTML_ROOT));
    private static final String DEFAULT_FILE = "index.html";
    private static final String DEFAULT_FILE_EXTENSION = ".html";

    private final Logger logger;

    static {
        String path = BoardBackend.getConfig().getString(BoardBackendConfigKeys.MIME_TYPES_FILE_PATH);
        try {
            MimeTypes.load(new FileInputStream(path));
        } catch (IOException e) {
            InputStream inputStream = HttpServerHandler.class.getResourceAsStream(path.startsWith("/") ? "" : "/" + path); //always use root of classpath
            if (inputStream != null) {
                try {
                    MimeTypes.load(inputStream);
                } catch (IOException e1) {
                    e.printStackTrace();
                }
            } else {
                e.printStackTrace(); // non critical, MimeTypes will just use the default value
            }
        }
    }

    public HttpServerHandler(Logger logger) {
        this.logger = logger;
    }

    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    public static final int HTTP_CACHE_SECONDS = 60;

    private FullHttpRequest request;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        this.request = request;
        logger.info(request.method() + " " + request.uri());
        if (!request.decoderResult().isSuccess()) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        if (!HttpMethod.GET.equals(request.method())) {
            sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }

        final boolean keepAlive = HttpUtil.isKeepAlive(request);
        final String uri = getUri(request.uri());
        if (uri == null) {
            sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }
        final String[] splitQuery = splitQuery(uri);
        final String rawPath = splitQuery[0];
        final String rawQuery = splitQuery[1];

        if (rawPath.endsWith(DEFAULT_FILE)) { // /index.html -> /
            sendRedirect(ctx, rawPath.substring(0, rawPath.length() - 10) + rawQuery);
            return;
        } else if (splitQuery[0].endsWith(DEFAULT_FILE_EXTENSION)) { // /page.html -> /page
            sendRedirect(ctx, rawPath.substring(0, rawPath.length() - 5) + rawQuery);
            return;
        }

        final String path = getPath(uri);
        if (path == null) {
            sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }

        File file = new File(HTTP_ROOT, path);
        if (file.isHidden() || !file.exists() || file.isDirectory() || !file.isFile()) {
            sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }



        // Cache Validation
        String ifModifiedSince = request.headers().get(HttpHeaderNames.IF_MODIFIED_SINCE);
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
            Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);

            // Only compare up to the second because the datetime format we send to the client
            // does not have milliseconds
            long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
            long fileLastModifiedSeconds = file.lastModified() / 1000;
            if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_MODIFIED, Unpooled.EMPTY_BUFFER);
                authenticate(ctx, response, file);
                setDateHeader(response);

                sendAndCleanupConnection(ctx, response);
                return;
            }
        }

        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException ignore) {
            sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }
        long fileLength = raf.length();

        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        authenticate(ctx, response, file);
        HttpUtil.setContentLength(response, fileLength);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, MimeTypes.getMimeType(file));
        setDateAndCacheHeaders(response, file);

        if (!keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        } else if (request.protocolVersion().equals(HttpVersion.HTTP_1_0)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        // Write the initial line and the header.
        ctx.write(response);

        // HttpChunkedInput will write the end marker (LastHttpContent) for us.
        ChannelFuture lastContentFuture = ctx.writeAndFlush(new HttpChunkedInput(new ChunkedFile(raf, 0, fileLength, 8192)), ctx.newProgressivePromise());

        if (!keepAlive) {
            // Close the connection when the whole content is written out.
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }



    private void sendRedirect(ChannelHandlerContext ctx, String newUri) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.MOVED_PERMANENTLY, Unpooled.EMPTY_BUFFER);
        response.headers().set(HttpHeaderNames.LOCATION, newUri);

        sendAndCleanupConnection(ctx, response);
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

        sendAndCleanupConnection(ctx, response);
    }

    private void sendAndCleanupConnection(ChannelHandlerContext ctx, FullHttpResponse response) {
        sendAndCleanupConnection(ctx, response, HttpUtil.isKeepAlive(request));
    }

    /**
     * If Keep-Alive is disabled, attaches "Connection: close" header to the response
     * and closes the connection after the response being sent.
     */
    private void sendAndCleanupConnection(ChannelHandlerContext ctx, FullHttpResponse response, boolean keepAlive) {
        final FullHttpRequest request = this.request;
        HttpUtil.setContentLength(response, response.content().readableBytes());
        if (!keepAlive) {
            // We're going to close the connection as soon as the response is sent,
            // so we should also make it clear for the client.
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        } else if (request.protocolVersion().equals(HttpVersion.HTTP_1_0)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        ChannelFuture flushPromise = ctx.writeAndFlush(response);

        if (!keepAlive) {
            // Close the connection as soon as the response is sent.
            flushPromise.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * Sets the Date header for the HTTP response
     */
    private static void setDateHeader(FullHttpResponse response) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        Calendar time = new GregorianCalendar();
        response.headers().set(HttpHeaderNames.DATE, dateFormatter.format(time.getTime()));
    }

    /**
     * Sets the Date and Cache headers for the HTTP Response
     */
    private static void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        // Date header
        Calendar time = new GregorianCalendar();
        response.headers().set(HttpHeaderNames.DATE, dateFormatter.format(time.getTime()));

        // Add cache headers
        time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
        response.headers().set(HttpHeaderNames.EXPIRES, dateFormatter.format(time.getTime()));
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
        response.headers().set(HttpHeaderNames.LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
    }

    private void authenticate(ChannelHandlerContext ctx, HttpResponse response, File file) {
        if (file.getName().equals(PersistentHttpSession.LOGIN_PATH)) {
            if (!authenticate(request, response)) {
                logger.info("Bad authentication");
                sendAndCleanupConnection(ctx, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED), false);
                //todo rate limiting strategies
            } else {
                logger.info("Good authentication");
            }
        }
    }

    private static boolean authenticate(HttpRequest request, HttpResponse response) {
        HttpSession.ClientCookie cookie = HttpSession.ClientCookie.getClientCookie(request, HttpSession.COOKIE_NAME);
        if (cookie == null) {
            User user;
            HttpSession.ClientCookie cookiePersistent = HttpSession.ClientCookie.getClientCookie(request, PersistentHttpSession.COOKIE_NAME);
            if (cookiePersistent != null) {
                PersistentHttpSession persistentHttpSession = BoardBackend.getDatabase().getAndRemovePersistentHttpSession(cookiePersistent.getId());
                if (persistentHttpSession != null && persistentHttpSession.validate(cookiePersistent.getToken())) {
                    user = BoardBackend.getDatabase().getUser(persistentHttpSession.getUser());
                } else {
                    return false;
                }
            } else {
                user = BoardBackend.getDatabase().createUser();
            }

            HttpSession httpSession = new HttpSession(user, response);
            BoardBackend.getDatabase().addHttpSession(httpSession);

            //this is single use and always refreshed
            PersistentHttpSession persistentHttpSession = new PersistentHttpSession(httpSession, response);
            BoardBackend.getDatabase().addPersistentHttpSession(persistentHttpSession);
            return true;
        } else {
            HttpSession httpSession = BoardBackend.getDatabase().getHttpSession(cookie.getId());
            return httpSession != null && httpSession.validate(cookie.getToken());
        }
    }


    private static final Pattern ALLOWED_CHARACTERS = Pattern.compile("^[/.?&=a-zA-Z0-9\\-_]+$");

    public static String getUri(String uri) {
        return (ALLOWED_CHARACTERS.matcher(uri).matches()) ? uri : null;
    }

    private static final Pattern ALLOWED_PATH = Pattern.compile("^[\\" + File.separatorChar + ".a-zA-Z0-9\\-_]+$");

    public static String getPath(String path) {
        path = path.replace("/", File.separator);

        if (path.contains(File.separator + '.') ||
                path.contains('.' + File.separator) ||
                path.charAt(0) == '.' || path.charAt(path.length() - 1) == '.' ||
                !ALLOWED_PATH.matcher(path).matches()) {
            return null;
        }

        if (path.endsWith(File.separator)) { // / -> index.html
            path = path + DEFAULT_FILE;
        } else if (path.lastIndexOf(File.separator) > path.lastIndexOf(".")) { // /page -> /page.html
            path = path + DEFAULT_FILE_EXTENSION;
        }
        return path;
    }


    /**
     * Returns String array with length of 2, with the first element as the path and the second element as the raw query
     */
    public static String[] splitQuery(String uri) {
        int index = uri.lastIndexOf("?");
        if (index <= 0 || uri.indexOf("?") != index) return new String[] {uri, ""}; // make sure there is only one ? in the uri

        return new String[] {uri.substring(0, index), uri.substring(index + 1)};
    }


    public static Map<String, String> parseQuery(String query) {
        Map<String, String> queries = new HashMap<>();
        String[] keyValuePairs = query.split("&");
        for (String keyValuePair : keyValuePairs) {
            String[] split = keyValuePair.split("=");
            if (split.length != 2) { // check if malformed
                return Collections.emptyMap();
            }
            queries.put(split[0], split[1]);
        }
        return queries;
    }
}