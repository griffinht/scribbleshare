package net.stzups.scribbleshare.backend.server.http;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.stream.ChunkedStream;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.data.database.ScribbleshareDatabase;
import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.Resource;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpConfig;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpSessionCookie;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSession;
import net.stzups.scribbleshare.data.objects.authentication.http.PersistentHttpUserSession;
import net.stzups.scribbleshare.data.objects.authentication.login.Login;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static net.stzups.scribbleshare.server.http.HttpUtils.send;
import static net.stzups.scribbleshare.server.http.HttpUtils.sendChunkedResource;
import static net.stzups.scribbleshare.server.http.HttpUtils.sendFile;
import static net.stzups.scribbleshare.server.http.HttpUtils.sendRedirect;

@ChannelHandler.Sharable
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    public interface Config extends HttpConfig {
        String getHttpRoot();
        int getHttpCacheSeconds();
        String getMimeTypesFilePath();
        String getDebugJsRoot();
    }
    private static final long MAX_AGE_NO_EXPIRE = 31536000;//one year, max age of a cookie


    private static final String DEFAULT_FILE = "index.html"; // / -> /index.html
    private static final String DEFAULT_FILE_EXTENSION = ".html"; // /index -> index.html

    // /index?key=value&otherKey=value
    private static final String QUERY_DELIMITER = "?";
    private static final String QUERY_SEPARATOR = "&";
    private static final String QUERY_PAIR_SEPARATOR = "=";
    private static final String QUERY_REGEX = QUERY_DELIMITER + QUERY_SEPARATOR + QUERY_PAIR_SEPARATOR;

    // abc-ABC_123.file
    private static final String FILE_NAME_REGEX = "a-zA-Z0-9-_";


    private static final String LOGIN_PAGE = "/login"; // the login page, where login requests should come from
    private static final String LOGIN_PATH = PersistentHttpUserSession.LOGIN_PATH; // where login requests should go
    private static final String LOGIN_SUCCESS = "/"; // redirect for a good login, should be the main page

    private static final String REGISTER_PAGE = "/register"; // the register page, where register requests should come from
    private static final String REGISTER_PATH = "/register"; // where register requests should go
    private static final String REGISTER_SUCCESS = LOGIN_PAGE; // redirect for a good register, should be the login page

    private static final String LOGOUT_PAGE = "/logout"; // the logout page, where logout requests should come from
    private static final String LOGOUT_PATH = "/logout"; // where logout requests should go
    private static final String LOGOUT_SUCCESS = LOGIN_PAGE; // redirect for a good logout, should be the login page

    private final HttpConfig config;
    private final ScribbleshareDatabase database;

    private final File jsRoot;
    private final File httpRoot;
    private final int httpCacheSeconds;
    private final MimeTypes mimeTypes = new MimeTypes();

    public HttpServerHandler(Config config, ScribbleshareDatabase database) {
        this.config = config;
        this.database = database;

        httpRoot = new File(config.getHttpRoot());
        if (config.getDebugJsRoot().equals("")) {
            jsRoot = httpRoot;
        } else {
            jsRoot = new File(config.getDebugJsRoot());
        }
        httpCacheSeconds = config.getHttpCacheSeconds();
        String path = config.getMimeTypesFilePath();
        // check for mime types in working directory
        try (FileInputStream fileInputStream = new FileInputStream(path)) {
            mimeTypes.load(fileInputStream);
        } catch (IOException e) {
            // check for mime types in root of classpath
            InputStream inputStream = HttpServerHandler.class.getResourceAsStream(path.startsWith("/") ? "" : "/" + path);
            if (inputStream != null) {
                try {
                    mimeTypes.load(inputStream);
                } catch (IOException e1) {
                    e.printStackTrace();
                }
            } else {
                e.printStackTrace(); // non critical, server will just serve 404 responses
            }
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        Scribbleshare.getLogger(ctx).info(request.method() + " " + request.uri());

        // sanitize uri
        final String uri = getUri(request.uri());
        if (uri == null) {
            send(ctx, request, HttpResponseStatus.NOT_FOUND);
            return;
        }

        // split query from path
        final String[] splitQuery = splitQuery(uri);
        if (splitQuery == null) {
            send(ctx, request, HttpResponseStatus.NOT_FOUND);
            return;
        }
        final String path = splitQuery[0];
        final String rawQuery = splitQuery[1];

        Map<String, String> queries = parseQuery(rawQuery);
        if (queries == null) {
            send(ctx, request, HttpResponseStatus.NOT_FOUND);
            return;
        }

        String[] route = getRoute(path);
        if (route == null) {
            send(ctx, request, HttpResponseStatus.NOT_FOUND);
            return;
        }

        // check if this is a special request

        switch (route[0]) {
            case "api": {
                if (route.length < 2) {
                    send(ctx, request, HttpResponseStatus.NOT_FOUND);
                    return;
                }

                switch (route[1]) {
                    case "document": {
                        if (route.length != 3 && route.length != 4) {
                            break;
                        }

                        Long userId = authenticate(ctx, request);
                        if (userId == null) {
                            send(ctx, request, HttpResponseStatus.UNAUTHORIZED);
                            return;
                        }

                        User user = database.getUser(userId);
                        if (user == null) {
                            Scribbleshare.getLogger(ctx).warning("User with " + userId + " authenticated but does not exist");
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
                                //todo
                                send(ctx, request, HttpResponseStatus.NOT_FOUND);
                                return;
                                /*Resource resource = BackendServerInitializer.getDatabase(ctx).getResource(documentId, documentId);
                                if (resource == null) { //indicates an empty unsaved canvas, so serve that
                                    send(ctx, request, Canvas.getEmptyCanvas());
                                    return;
                                }
                                HttpHeaders headers = new DefaultHttpHeaders();
                                headers.set(HttpHeaderNames.CACHE_CONTROL, "private,max-age=0");//cache but always revalidate
                                sendChunkedResource(ctx, request, headers, new ChunkedStream(new ByteBufInputStream(resource.getData())), resource.getLastModified());//todo don't fetch entire document from db if not modified*/
                            } else if (request.method().equals(HttpMethod.POST)) { //todo validation/security for submitted resources
                                Document document = database.getDocument(documentId);
                                if (document == null) {
                                    Scribbleshare.getLogger(ctx).warning("Document with id " + documentId + " for user " + user + " somehow does not exist");
                                    break;
                                }
                                send(ctx, request, Unpooled.copyLong(database.addResource(document.getId(), new Resource(request.content()))));
                            } else {
                                send(ctx, request, HttpResponseStatus.METHOD_NOT_ALLOWED);
                            }
                        } else { // route.length == 4, get resource from document
                            // does the document have this resource?
                            long resourceId;
                            try {
                                resourceId = Long.parseLong(route[3]);
                            } catch (NumberFormatException e) {
                                break;
                            }

                            Document document = database.getDocument(documentId);
                            if (document == null) {
                                Scribbleshare.getLogger(ctx).warning("Document with id " + documentId + " for user " + user + " somehow does not exist");
                                break;
                            }

                            if (request.method().equals(HttpMethod.GET)) {
                                // get resource, resource must exist on the document
                                Resource resource = database.getResource(resourceId, documentId);
                                if (resource == null) {
                                    break;
                                }

                                HttpHeaders headers = new DefaultHttpHeaders();
                                headers.add(HttpHeaderNames.CACHE_CONTROL, "private,max-age=" + MAX_AGE_NO_EXPIRE + ",immutable");//cache and never revalidate - permanent
                                sendChunkedResource(ctx, request, headers, new ChunkedStream(new ByteBufInputStream(resource.getData())));
                            } else {
                                send(ctx, request, HttpResponseStatus.METHOD_NOT_ALLOWED);
                            }
                        }
                        return;
                    }
                }
                send(ctx, request, HttpResponseStatus.NOT_FOUND);
                return;
            }

        }

        //login

        //todo validate for extra fields that should not happen
        if (uri.equals(LOGIN_PATH) && request.method().equals(HttpMethod.POST)) {
            Map<String, String> form = parseForm(request);
            if (form == null) {
                send(ctx, request, HttpResponseStatus.BAD_REQUEST);
                return;
            }

            // validate

            String username = form.get("username");
            String password = form.get("password");

            if (username == null || password == null) {
                send(ctx, request, HttpResponseStatus.BAD_REQUEST);
                return;
            }

            String rememberRaw = form.get("remember");
            boolean remember;
            if (rememberRaw != null) {
                if (rememberRaw.equals("on")) {
                    remember = true;
                } else {
                    send(ctx, request, HttpResponseStatus.BAD_REQUEST);
                    return;
                }
            } else {
                remember = false;
            }


            System.out.println(username + ", " + password + ", " + remember);

            Login login = database.getLogin(username);
            Long id = Login.verify(login, password.getBytes(StandardCharsets.UTF_8));
            if (id == null) {
                //todo rate limit and generic error handling
                if (login == null) {
                    Scribbleshare.getLogger(ctx).info("Bad username " + username);
                } else {
                    Scribbleshare.getLogger(ctx).info("Bad password for username " + username);
                }

                sendRedirect(ctx, request, LOGIN_PAGE);
                return;
            }

            HttpHeaders httpHeaders = new DefaultHttpHeaders();
            database.addHttpSession(new HttpUserSession(config, database.getUser(login.getId()), httpHeaders));
            sendRedirect(ctx, request, httpHeaders, LOGIN_SUCCESS);
            return;
        } else if (uri.equals(REGISTER_PATH) && request.method().equals(HttpMethod.POST)) {
            Map<String, String> form = parseForm(request);
            if (form == null) {
                send(ctx, request, HttpResponseStatus.BAD_REQUEST);
                return;
            }

            // validate

            String username = form.get("username");
            String password = form.get("password");

            if (username == null || password == null) {
                send(ctx, request, HttpResponseStatus.BAD_REQUEST);
                return;
            }

            //todo validate
            if (false) {
                //todo rate limit and generic error handling

                sendRedirect(ctx, request, REGISTER_PAGE);
                return;
            }

            //todo check for existing username and user with username

            User user;
            HttpSessionCookie cookie = HttpUserSession.getCookie(request);
            if (cookie != null) {
                HttpUserSession httpSession = database.getHttpSession(cookie.getId());
                if (httpSession != null) {
                    user = database.getUser(httpSession.getUser());
                } else {
                    user = new User(username);
                    database.addUser(user);
                }
            } else {
                user = new User(username);
                database.addUser(user);
            }
            Login login = new Login(user, password.getBytes(StandardCharsets.UTF_8));
            database.addLogin(login);

            System.out.println(username + ", register " + password);

            sendRedirect(ctx, request, REGISTER_SUCCESS);
            return;
        } else if (uri.equals(LOGOUT_PAGE) && request.method().equals(HttpMethod.POST)) {
            Map<String, String> form = parseForm(request);
            if (form == null) {
                send(ctx, request, HttpResponseStatus.BAD_REQUEST);
                return;
            }

            HttpHeaders headers = new DefaultHttpHeaders();
            //todo authenticate so only authenticated users can log out
            HttpSessionCookie cookie = HttpUserSession.getCookie(request);
            if (cookie != null) {
                database.expireHttpSession(cookie.getId());
                //todo clear cookie
            }
            HttpSessionCookie persistentCookie = PersistentHttpUserSession.getCookie(request);
            if (persistentCookie != null) {
                database.expireHttpSession(persistentCookie.getId());
                //todo clear cookie
            }


            sendRedirect(ctx, request, LOGIN_SUCCESS);
            return;
        }

        // otherwise try to serve a regular HTTP file resource

        if (!HttpMethod.GET.equals(request.method())) {
            send(ctx, request, HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
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

        // get filesystem filePath from provided filePath
        final String filePath = getFilePath(path);
        if (filePath == null) {
            send(ctx, request, HttpResponseStatus.NOT_FOUND);
            return;
        }

        File root;
        if (filePath.endsWith(".js")) {
            root = jsRoot;
        } else {
            root = httpRoot;
        }
        File file = new File(root, filePath);
        if (file.isHidden() || !file.exists() || file.isDirectory() || !file.isFile()) {
            if (new File(httpRoot, filePath.substring(0, filePath.length() - DEFAULT_FILE_EXTENSION.length())).isDirectory()) { // /test -> /test/ if test is a valid directory and /test.html does not exist
                sendRedirect(ctx, request, path + "/" + rawQuery);
            } else {
                send(ctx, request, HttpResponseStatus.NOT_FOUND);
            }
            return;
        }
        HttpHeaders headers = new DefaultHttpHeaders();
 /*       if (path.equals(PersistentHttpUserSession.LOGIN_PATH)) {
            logIn(ctx, config, request, headers);
        }*/
        headers.set(HttpHeaderNames.CACHE_CONTROL, "public,max-age=" + httpCacheSeconds);//cache but revalidate if stale todo set to private cache for resources behind authentication
        sendFile(ctx, request, headers, file, mimeTypes.getMimeType(file));
    }

    private Long authenticate(ChannelHandlerContext ctx, FullHttpRequest request) {
        HttpSessionCookie cookie = HttpUserSession.getCookie(request);
        if (cookie != null) {
            HttpUserSession httpSession = database.getHttpSession(cookie.getId());
            if (httpSession != null && httpSession.validate(cookie)) {
                Scribbleshare.getLogger(ctx).info("Authenticated with id " + httpSession.getUser());
                return httpSession.getUser();
            } else {
                Scribbleshare.getLogger(ctx).warning("Bad authentication");
                //bad authentication attempt
                //todo rate limit timeout server a proper response???
            }
        } else {
            Scribbleshare.getLogger(ctx).info("No authentication");
        }

        return null;
    }

    private void logIn(ChannelHandlerContext ctx, HttpConfig config, FullHttpRequest request, HttpHeaders headers) {
        if (!logIn(config, request, headers)) {
            Scribbleshare.getLogger(ctx).warning("Bad authentication");
            send(ctx, request, HttpResponseStatus.UNAUTHORIZED);
            //todo rate limiting strategies
        } else {
            Scribbleshare.getLogger(ctx).info("Good authentication");
        }
    }

    private boolean logIn(HttpConfig config, HttpRequest request, HttpHeaders headers) {
        HttpSessionCookie cookie = HttpUserSession.getCookie(request);
        if (cookie == null) {
            User user;
            HttpSessionCookie cookiePersistent = PersistentHttpUserSession.getCookie(request);
            if (cookiePersistent != null) {
                PersistentHttpUserSession persistentHttpSession = database.getAndExpirePersistentHttpSession(cookiePersistent.getId());
                if (persistentHttpSession != null && persistentHttpSession.validate(cookiePersistent)) {
                    user = database.getUser(persistentHttpSession.getUser());
                } else {
                    //return false; todo
                    user = new User();
                    database.addUser(user);
                }
            } else {
                user = new User();
                database.addUser(user);
            }

            HttpUserSession httpSession = new HttpUserSession(config, user, headers);
            database.addHttpSession(httpSession);

            //this is single use and always refreshed
            PersistentHttpUserSession persistentHttpSession = new PersistentHttpUserSession(config, httpSession, headers);
            database.addPersistentHttpSession(persistentHttpSession);
            return true;
        } else {
            HttpUserSession httpSession = database.getHttpSession(cookie.getId());
            if (httpSession != null && httpSession.validate(cookie)) {
                return true;
            } else {
                //todo copied and bad
                User user = new User();
                database.addUser(user);
                httpSession = new HttpUserSession(config, user, headers);
                database.addHttpSession(httpSession);

                //this is single use and always refreshed
                PersistentHttpUserSession persistentHttpSession = new PersistentHttpUserSession(config, httpSession, headers);
                database.addPersistentHttpSession(persistentHttpSession);
                return true;
            }
        }
    }



    private static final Pattern ALLOWED_CHARACTERS = Pattern.compile("^[/." + QUERY_REGEX + FILE_NAME_REGEX + "]+$");

    /** Sanitizes uri */
    public static String getUri(String uri) {
        return (ALLOWED_CHARACTERS.matcher(uri).matches()) ? uri : null;
    }

    private static final Pattern ALLOWED_PATH = Pattern.compile("^[\\\\" + File.separator + "." + FILE_NAME_REGEX + "]+$");

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
     * /index.html?key=value&otherKey=otherValue -> [ /index.html, key=value&otherKey=otherValue ]
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
            return new String[] {uri.substring(0, index), uri.substring(index + 1)};
        }
    }

    /**
     * Parses key=value&otherKey=otherValue&keyWithEmptyValue to a Map of key-value pairs
     */
    public static Map<String, String> parseQuery(String query) {
        if (query.isEmpty()) return Collections.emptyMap(); // no query to parse

        Map<String, String> queries = new HashMap<>();
        String[] keyValuePairs = query.split(QUERY_SEPARATOR);
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

    public static Map<String, String> parseForm(FullHttpRequest request) {
        String contentType = request.headers().get(HttpHeaderNames.CONTENT_TYPE);
        if (contentType == null || !contentType.contentEquals(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED)) {
            return null;
        }

        //todo check host/origin/referer to make sure they originate from LOGIN_PAGE

        return parseQuery(request.content().toString(StandardCharsets.UTF_8));
    }
}