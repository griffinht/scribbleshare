package net.stzups.scribbleshare.backend.server.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
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
import net.stzups.scribbleshare.backend.ScribbleshareBackend;
import net.stzups.scribbleshare.backend.ScribbleshareBackendConfigKeys;
import net.stzups.scribbleshare.backend.server.ServerInitializer;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.data.objects.session.HttpSession;
import net.stzups.scribbleshare.data.objects.session.PersistentHttpSession;

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
import java.util.regex.Pattern;

@ChannelHandler.Sharable
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final File HTTP_ROOT = new File(ScribbleshareBackend.getConfig().getString(ScribbleshareBackendConfigKeys.HTML_ROOT));
    private static final int HTTP_CACHE_SECONDS = ScribbleshareBackend.getConfig().getInteger(ScribbleshareBackendConfigKeys.HTTP_CACHE_SECONDS);

    private static final String DEFAULT_FILE = "index.html";
    private static final String DEFAULT_FILE_EXTENSION = ".html";

    private static final String QUERY_DELIMITER = "?";
    private static final String QUERY_SEPARATOR = "&";
    private static final String QUERY_PAIR_SEPARATOR = "=";

    private static final String QUERY_REGEX = QUERY_DELIMITER + QUERY_SEPARATOR + QUERY_PAIR_SEPARATOR;
    private static final String FILE_NAME_REGEX = "a-zA-Z0-9-_";

    static {
        String path = ScribbleshareBackend.getConfig().getString(ScribbleshareBackendConfigKeys.MIME_TYPES_FILE_PATH);
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

    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        ServerInitializer.getLogger(ctx).info(request.method() + " " + request.uri());
        if (!request.decoderResult().isSuccess()) {
            sendError(ctx, request, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        if (!HttpMethod.GET.equals(request.method())) {
            sendError(ctx, request, HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }

        final boolean keepAlive = HttpUtil.isKeepAlive(request);
        // sanitize uri
        final String uri = getUri(request.uri());
        if (uri == null) {
            sendError(ctx, request, HttpResponseStatus.NOT_FOUND);
            return;
        }

        // split query from path
        final String[] splitQuery = splitQuery(uri);
        if (splitQuery == null) {
            sendError(ctx, request, HttpResponseStatus.NOT_FOUND);
            return;
        }
        final String path = splitQuery[0];
        final String rawQuery = splitQuery[1];

        Map<String, String> queries = parseQuery(rawQuery);
        if (queries == null) {
            sendError(ctx, request, HttpResponseStatus.NOT_FOUND);
        }

        // redirects
        if (path.endsWith(DEFAULT_FILE)) { // /index.html -> /
            sendRedirect(ctx, request, path.substring(0, path.length() - DEFAULT_FILE.length()) + rawQuery);
            return;
        } else if ((path + DEFAULT_FILE_EXTENSION).endsWith(DEFAULT_FILE)) { // /index -> /
            sendRedirect(ctx, request, path.substring(0, path.length() - (DEFAULT_FILE.length() - DEFAULT_FILE_EXTENSION.length())) + rawQuery);
            return;
        } else if (path.endsWith(DEFAULT_FILE_EXTENSION)) { // /page.html -> /page
            sendRedirect(ctx, request, path.substring(0, path.length() - DEFAULT_FILE_EXTENSION.length()) + rawQuery);
            return;
        }

        String[] route = getRoute(path);
        if (route == null) {
            sendError(ctx, request, HttpResponseStatus.NOT_FOUND);
            return;
        }

        // check if this is a special request

        switch (route[0]) {
            case "api":
                if (route.length > 1) {
                    switch (route[1]) {
                        case "document":
                            if (route.length == 3) {
                                long id;
                                try {
                                    id = Long.parseLong(route[2]);
                                } catch (NumberFormatException e) {
                                    sendError(ctx, request, HttpResponseStatus.NOT_FOUND);
                                    return;
                                }
                                System.out.println(ScribbleshareBackend.getDatabase().getCanvas(id));
                            }
                    }
                } else {
                    sendError(ctx, request, HttpResponseStatus.NOT_FOUND);
                }
                return;
        }

        // otherwise try to serve a regular HTTP file resource

        // get filesystem filePath from provided filePath
        final String filePath = getFilePath(path);
        if (filePath == null) {
            sendError(ctx, request, HttpResponseStatus.NOT_FOUND);
            return;
        }

        File file = new File(HTTP_ROOT, filePath);
        if (file.isHidden() || !file.exists() || file.isDirectory() || !file.isFile()) {
            if (new File(HTTP_ROOT, filePath.substring(0, filePath.length() - DEFAULT_FILE_EXTENSION.length())).isDirectory()) { // /test -> /test/ if test is a valid directory and /test.html does not exist
                sendRedirect(ctx, request, path + "/" + rawQuery);
            } else {
                sendError(ctx, request, HttpResponseStatus.NOT_FOUND);
                return;
            }
        }

        String mimeType = MimeTypes.getMimeType(file);
        if (mimeType == null) {
            ServerInitializer.getLogger(ctx).warning("Unknown MIME type for file " + file.getName());
            sendError(ctx, request, HttpResponseStatus.NOT_FOUND);
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
                if (uri.equals(PersistentHttpSession.LOGIN_PATH)) {
                    authenticate(ctx, request, response);
                }
                setDateHeader(response);

                sendAndCleanupConnection(ctx, request, response);
                return;
            }
        }

        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException ignore) {
            sendError(ctx, request, HttpResponseStatus.NOT_FOUND);
            return;
        }
        long fileLength = raf.length();

        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        if (uri.equals(PersistentHttpSession.LOGIN_PATH)) {
            authenticate(ctx, request, response);
        }
        HttpUtil.setContentLength(response, fileLength);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, mimeType);
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
            sendError(ctx, null, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }



    private static void sendRedirect(ChannelHandlerContext ctx, FullHttpRequest request, String newUri) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.MOVED_PERMANENTLY, Unpooled.EMPTY_BUFFER);
        response.headers().set(HttpHeaderNames.LOCATION, newUri);

        sendAndCleanupConnection(ctx, request, response);
    }

    private static void sendError(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

        sendAndCleanupConnection(ctx, request, response);
    }

    /**
     * If Keep-Alive is disabled, attaches "Connection: close" header to the response
     * and closes the connection after the response being sent.
     */
    private static void sendAndCleanupConnection(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response) {
        HttpUtil.setContentLength(response, response.content().readableBytes());
        if (request == null) {
            ctx.writeAndFlush(response);
            return;
        }

        boolean keepAlive = !HttpUtil.isKeepAlive(request);
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

    private void authenticate(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponse response) {
        if (!authenticate(request, response)) {
            ServerInitializer.getLogger(ctx).info("Bad authentication");
            sendAndCleanupConnection(ctx, null, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED));
            //todo rate limiting strategies
        } else {
            ServerInitializer.getLogger(ctx).info("Good authentication");
        }
    }

    private static boolean authenticate(HttpRequest request, HttpResponse response) {
        HttpSession.ClientCookie cookie = HttpSession.ClientCookie.getClientCookie(request, HttpSession.COOKIE_NAME);
        if (cookie == null) {
            User user;
            HttpSession.ClientCookie cookiePersistent = HttpSession.ClientCookie.getClientCookie(request, PersistentHttpSession.COOKIE_NAME);
            if (cookiePersistent != null) {
                PersistentHttpSession persistentHttpSession = ScribbleshareBackend.getDatabase().getAndRemovePersistentHttpSession(cookiePersistent.getId());
                if (persistentHttpSession != null && persistentHttpSession.validate(cookiePersistent.getToken())) {
                    user = ScribbleshareBackend.getDatabase().getUser(persistentHttpSession.getUser());
                } else {
                    return false;
                }
            } else {
                user = ScribbleshareBackend.getDatabase().createUser();
            }

            HttpSession httpSession = new HttpSession(user, response);
            ScribbleshareBackend.getDatabase().addHttpSession(httpSession);

            //this is single use and always refreshed
            PersistentHttpSession persistentHttpSession = new PersistentHttpSession(httpSession, response);
            ScribbleshareBackend.getDatabase().addPersistentHttpSession(persistentHttpSession);
            return true;
        } else {
            HttpSession httpSession = ScribbleshareBackend.getDatabase().getHttpSession(cookie.getId());
            return httpSession != null && httpSession.validate(cookie.getToken());
        }
    }


    private static final Pattern ALLOWED_CHARACTERS = Pattern.compile("^[/." + QUERY_REGEX + FILE_NAME_REGEX + "]+$");

    /** Sanitizes uri */
    public static String getUri(String uri) {
        return (ALLOWED_CHARACTERS.matcher(uri).matches()) ? uri : null;
    }

    private static final Pattern ALLOWED_PATH = Pattern.compile("^[\\" + File.separator + "." + FILE_NAME_REGEX + "]+$");

    private static String[] getRoute(String path) {
        if (!path.startsWith("/")) return null;
        String[] route = path.substring(1).split("/");
        if (route.length == 0) {
            return new String[] {""};
        } else {
            return route;
        }
    }

    /** Converts uri to filesystem path */
    private static String getFilePath(String path) {
        path = path.replace("/", File.separator);

        if (path.contains(File.separator + '.') // /.
                || path.contains('.' + File.separator) // ./
                || path.contains(File.separator + File.separator) // //
                || path.charAt(0) == '.' // .
                || path.charAt(path.length() - 1) == '.' // /page.
                || !ALLOWED_PATH.matcher(path).matches()) {
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
     * Example:
     * /index.html?key=value&otherKey=otherValue -> [ /index.html, ?key=value&otherKey=otherValue ]
     */
    public static String[] splitQuery(String uri) {
        int index = uri.lastIndexOf(QUERY_DELIMITER);
        if (index <= 0) { // check for a query
            if (uri.contains(QUERY_SEPARATOR) || uri.contains(QUERY_PAIR_SEPARATOR)) { // there is no query, so there should also be no other reserved keywords
                return null;
            } else {
                return new String[] {uri, ""};
            }
        } else if (uri.indexOf(QUERY_DELIMITER) != index) { // make sure there is only one ? in the uri
            return null;
        } else {
            return new String[] {uri.substring(0, index), uri.substring(index)};
        }
    }

    /**
     * Parses ?key=value&otherKey=otherValue&keyWithEmptyValue to a Map of key-value pairs
     */
    public static Map<String, String> parseQuery(String query) {
        if (query.isEmpty()) return Collections.emptyMap(); // no query to parse
        if (!query.startsWith("?")) return null; // malformed, should start with ?

        Map<String, String> queries = new HashMap<>();
        String[] keyValuePairs = query.substring(1) // query starts with ?
                .split(QUERY_SEPARATOR);
        for (String keyValuePair : keyValuePairs) {
            String[] split = keyValuePair.split(QUERY_PAIR_SEPARATOR, 3); // a limit of 2 (expected) would not detect malformed queries such as ?key==, so we need to go one more
            if (split.length == 1) { // key with no value, such as ?key
                queries.put(split[0], "");
            } else if (split.length != 2) { // malformed, each key should have one value
                return null;
            } else {
                queries.put(split[0], split[1]);
            }
        }

        return queries;
    }
}