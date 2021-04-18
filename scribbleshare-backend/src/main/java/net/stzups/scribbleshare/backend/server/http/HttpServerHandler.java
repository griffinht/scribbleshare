package net.stzups.scribbleshare.backend.server.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedInput;
import io.netty.handler.stream.ChunkedStream;
import io.netty.util.CharsetUtil;
import net.stzups.scribbleshare.backend.ScribbleshareBackend;
import net.stzups.scribbleshare.backend.ScribbleshareBackendConfigKeys;
import net.stzups.scribbleshare.backend.server.ServerInitializer;
import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.Resource;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.data.objects.canvas.Canvas;
import net.stzups.scribbleshare.data.objects.session.HttpSession;
import net.stzups.scribbleshare.data.objects.session.PersistentHttpSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Collections;
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

    private static final long MAX_AGE_NO_EXPIRE = 31536000;//one year
    private static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private static final String HTTP_DATE_GMT_TIMEZONE = "GMT";

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

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        ServerInitializer.getLogger(ctx).info(request.method() + " " + request.uri());
        if (!request.decoderResult().isSuccess()) {
            sendError(ctx, request, HttpResponseStatus.BAD_REQUEST);
            return;
        }

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

        String[] route = getRoute(path);
        if (route == null) {
            sendError(ctx, request, HttpResponseStatus.NOT_FOUND);
            return;
        }

        // check if this is a special request

        switch (route[0]) {
            case "api": {
                if (route.length < 2) {
                    sendError(ctx, request, HttpResponseStatus.NOT_FOUND);
                    return;
                }

                switch (route[1]) {
                    case "document": {
                        if (route.length != 3 && route.length != 4) {
                            break;
                        }

                        Long userId = authenticate(ctx, request);
                        if (userId == null) {
                            sendError(ctx, request, HttpResponseStatus.UNAUTHORIZED);
                            return;
                        }

                        User user = ScribbleshareBackend.getDatabase().getUser(userId);
                        if (user == null) {
                            ServerInitializer.getLogger(ctx).warning("User with " + userId + " authenticated but does not exist");
                            break;
                        }

                        long documentId;
                        try {
                            documentId = Long.parseLong(route[2]);
                        } catch (NumberFormatException e) {
                            break;
                        }

                        if (!user.getOwnedDocuments().contains(documentId) && !user.getSharedDocuments().contains(documentId)) {
                            break; //user cant do this to documents they don't have access to todo public documents
                        }

                        // user has access to the document

                        if (route.length == 3) { // get document or submit new resource to document
                            if (request.method().equals(HttpMethod.GET)) {
                                Resource resource = ScribbleshareBackend.getDatabase().getResource(documentId, documentId);
                                if (resource == null) { //indicates an empty unsaved canvas, so serve that
                                    send(ctx, request, Canvas.getEmptyCanvas());
                                    return;
                                }
                                HttpHeaders headers = new DefaultHttpHeaders();
                                headers.set(HttpHeaderNames.CACHE_CONTROL, "private,max-age=0");//cache but always revalidate
                                sendChunkedResource(ctx, request, headers, new ChunkedStream(new ByteBufInputStream(resource.getData())), resource.getLastModified());//todo don't fetch entire document from db if not modified
                            } else if (request.method().equals(HttpMethod.POST)) { //todo validation/security for submitted resources
                                Document document = ScribbleshareBackend.getDatabase().getDocument(documentId);
                                if (document == null) {
                                    ServerInitializer.getLogger(ctx).warning("Document with id " + documentId + " for user " + user + " somehow does not exist");
                                    break;
                                }
                                send(ctx, request, Unpooled.copyLong(ScribbleshareBackend.getDatabase().addResource(document.getId(), new Resource(request.content()))));
                            } else {
                                sendError(ctx, request, HttpResponseStatus.METHOD_NOT_ALLOWED);
                            }
                        } else { // route.length == 4, get resource from document
                            // does the document have this resource?
                            long resourceId;
                            try {
                                resourceId = Long.parseLong(route[3]);
                            } catch (NumberFormatException e) {
                                break;
                            }

                            Document document = ScribbleshareBackend.getDatabase().getDocument(documentId);
                            if (document == null) {
                                ServerInitializer.getLogger(ctx).warning("Document with id " + documentId + " for user " + user + " somehow does not exist");
                                break;
                            }

                            if (request.method().equals(HttpMethod.GET)) {
                                // get resource, resource must exist on the document
                                Resource resource = ScribbleshareBackend.getDatabase().getResource(resourceId, documentId);
                                if (resource == null) {
                                    break;
                                }

                                HttpHeaders headers = new DefaultHttpHeaders();
                                headers.add(HttpHeaderNames.CACHE_CONTROL, "private,max-age=" + MAX_AGE_NO_EXPIRE + ",immutable");//cache and never revalidate - permanent
                                sendChunkedResource(ctx, request, headers, new ChunkedStream(new ByteBufInputStream(resource.getData())));
                            } else {
                                sendError(ctx, request, HttpResponseStatus.METHOD_NOT_ALLOWED);
                            }
                        }
                        //a response should have been sent by the time this is reached
                        return;
                    }
                }
                sendError(ctx, request, HttpResponseStatus.NOT_FOUND);
                return;
            }

        }

        if (!HttpMethod.GET.equals(request.method())) {
            sendError(ctx, request, HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }

        // otherwise try to serve a regular HTTP file resource

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
        HttpHeaders headers = new DefaultHttpHeaders();
        if (uri.equals(PersistentHttpSession.LOGIN_PATH)) {
            logIn(ctx, request, headers);
        }
        headers.set(HttpHeaderNames.CACHE_CONTROL, "public,max-age=" + HTTP_CACHE_SECONDS);//cache but revalidate if stale todo set to private cache for resources behind authentication
        sendFile(ctx, request, headers, file);
    }

    private static void send(ChannelHandlerContext ctx, FullHttpRequest request, ByteBuf responseContent) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/octet-stream");
        response.content().writeBytes(responseContent);

        send(ctx, request, response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, null, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static boolean isModifiedSince(FullHttpRequest request, Timestamp lastModified) throws Exception {
        String ifModifiedSince = request.headers().get(HttpHeaderNames.IF_MODIFIED_SINCE);
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);//todo

            //round lastModified to nearest second and compare
            return Instant.ofEpochSecond(lastModified.getTime() / 1000).isAfter(dateFormatter.parse(ifModifiedSince).toInstant());
        }

        return true;
    }

    private static void setDateAndLastModified(HttpHeaders headers, Timestamp lastModified) {//todo
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        Calendar time = new GregorianCalendar();
        headers.set(HttpHeaderNames.DATE, dateFormatter.format(time.getTime()));

        headers.set(HttpHeaderNames.LAST_MODIFIED, dateFormatter.format(lastModified));
    }

    /** sets keep alive headers and returns whether the connection is keep alive */
    private static boolean setKeepAlive(FullHttpRequest request, HttpResponse response) {
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        if (!keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        } else if (request.protocolVersion().equals(HttpVersion.HTTP_1_0)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        return keepAlive;
    }



    private static void send(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response) {
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

    private static void send(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponse response, HttpChunkedInput httpChunkedInput) {
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

    private static void sendRedirect(ChannelHandlerContext ctx, FullHttpRequest request, String newUri) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.MOVED_PERMANENTLY, Unpooled.EMPTY_BUFFER);
        response.headers().set(HttpHeaderNames.LOCATION, newUri);

        send(ctx, request, response);
    }

    private static void sendError(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.EMPTY_BUFFER);
        //response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

        send(ctx, request, response);
    }

    /** sends if stale, otherwise sends not modified */
    private static void sendChunkedResource(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers, ChunkedInput<ByteBuf> chunkedInput, Timestamp lastModified) throws Exception {
        setDateAndLastModified(headers, lastModified);
        if (isModifiedSince(request, lastModified)) {
            ServerInitializer.getLogger(ctx).info("Uncached");
            sendChunkedResource(ctx, request, headers, chunkedInput);
        } else {
            ServerInitializer.getLogger(ctx).info("Cached");
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_MODIFIED, Unpooled.EMPTY_BUFFER);
            response.headers().set(headers);

            send(ctx, request, response);
        }
    }

    /** sends resource with headers */
    private static void sendChunkedResource(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers, ChunkedInput<ByteBuf> chunkedInput) {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(headers);

        send(ctx, request, response, new HttpChunkedInput(chunkedInput));
    }

    /** make sure the file being sent is valid */
    private static void sendFile(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers, File file) throws Exception {
        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException ignore) {
            sendError(ctx, request, HttpResponseStatus.NOT_FOUND);
            return;
        }
        long fileLength = raf.length();
        String mimeType = MimeTypes.getMimeType(file);
        if (mimeType == null) {
            ServerInitializer.getLogger(ctx).warning("Unknown MIME type for file " + file.getName());
            sendError(ctx, request, HttpResponseStatus.NOT_FOUND);
            return;
        }
        headers.set(HttpHeaderNames.CONTENT_TYPE, mimeType);

        sendChunkedResource(ctx, request, headers, new ChunkedFile(raf, 0, fileLength, 8192), Timestamp.from(Instant.ofEpochMilli(file.lastModified())));
    }


    private static Long authenticate(ChannelHandlerContext ctx, FullHttpRequest request) {
        HttpSession.ClientCookie cookie = HttpSession.ClientCookie.getClientCookie(request, HttpSession.COOKIE_NAME);
        if (cookie != null) {
            HttpSession httpSession = ScribbleshareBackend.getDatabase().getHttpSession(cookie.getId());
            if (httpSession != null && httpSession.validate(cookie.getToken())) {
                ServerInitializer.getLogger(ctx).info("Authenticated with id " + httpSession.getUser());
                return httpSession.getUser();
            } else {
                ServerInitializer.getLogger(ctx).warning("Bad authentication");
                //bad authentication attempt
                //todo rate limit timeout server a proper response???
            }
        } else {
            ServerInitializer.getLogger(ctx).info("No authentication");
        }

        return null;
    }

    private static void logIn(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers) {
        if (!logIn(request, headers)) {
            ServerInitializer.getLogger(ctx).warning("Bad authentication");
            sendError(ctx, request, HttpResponseStatus.UNAUTHORIZED);
            //todo rate limiting strategies
        } else {
            ServerInitializer.getLogger(ctx).info("Good authentication");
        }
    }

    private static boolean logIn(HttpRequest request, HttpHeaders headers) {
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

            HttpSession httpSession = new HttpSession(user, headers);
            ScribbleshareBackend.getDatabase().addHttpSession(httpSession);

            //this is single use and always refreshed
            PersistentHttpSession persistentHttpSession = new PersistentHttpSession(httpSession, headers);
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