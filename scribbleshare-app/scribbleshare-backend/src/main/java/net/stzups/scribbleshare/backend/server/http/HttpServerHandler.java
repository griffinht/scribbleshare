package net.stzups.scribbleshare.backend.server.http;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.stream.ChunkedStream;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.data.database.ScribbleshareDatabase;
import net.stzups.scribbleshare.data.database.exception.exceptions.FailedException;
import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.Resource;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.data.objects.authentication.AuthenticatedUserSession;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpConfig;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpSessionCookie;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSession;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSessionCookie;
import net.stzups.scribbleshare.data.objects.authentication.http.PersistentHttpUserSession;
import net.stzups.scribbleshare.data.objects.authentication.login.Login;
import net.stzups.scribbleshare.server.http.exception.HttpException;
import net.stzups.scribbleshare.server.http.exception.exceptions.BadRequestException;
import net.stzups.scribbleshare.server.http.exception.exceptions.InternalServerException;
import net.stzups.scribbleshare.server.http.exception.exceptions.NotFoundException;
import net.stzups.scribbleshare.server.http.exception.exceptions.UnauthorizedException;
import net.stzups.scribbleshare.server.http.handlers.HttpAuthenticator;
import net.stzups.scribbleshare.server.http.objects.Form;
import net.stzups.scribbleshare.server.http.objects.Route;
import net.stzups.scribbleshare.server.http.objects.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
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






    private static final String AUTHENTICATE_PAGE = "/"; // the page where new users will be automatically created

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
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        Scribbleshare.getLogger(ctx).info(request.method() + " " + request.uri());
        try {
            handle(ctx, request);
        } catch (HttpException e) {
            Scribbleshare.getLogger(ctx).log(Level.WARNING, "Exception while handling HTTP request", e);
            send(ctx, request, e.responseStatus());
        }
    }

    private void handle(ChannelHandlerContext ctx, FullHttpRequest request) throws HttpException {
        final Route route;
        try {
            route = new Route(request.uri());
        } catch (BadRequestException e) {
            throw new NotFoundException("Exception while parsing URI", e);
        }

        // check if this is a special request

        switch (route.get(0)) {
            case "api": {
                switch (route.get(1)) {
                    case "document": {
                        AuthenticatedUserSession session = HttpAuthenticator.authenticateHttpUserSession(request, database);

                        if (session == null) {
                            throw new UnauthorizedException("No authentication");
                        }

                        User user = session.getUser();

                        long documentId;
                        try {
                            documentId = Long.parseLong(route.get(2));
                        } catch (NumberFormatException e) {
                            throw new BadRequestException("Exception while parsing " + route.get(2), e);
                        }

                        if (!user.getOwnedDocuments().contains(documentId) && !user.getSharedDocuments().contains(documentId)) {
                            throw new NotFoundException("User tried to open document they don't have access to");
                            //todo public documents
                        }

                        // user has access to the document

                        if (route.length() == 3) { // get document or submit new resource to document
                            if (request.method().equals(HttpMethod.GET)) {
                                //todo
                                throw new NotFoundException("todo not implemented yet");
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
                                if (document == null)
                                    throw new NotFoundException("Document with id " + documentId + " for user " + user + " somehow does not exist");

                                send(ctx, request, Unpooled.copyLong(database.addResource(document.getId(), new Resource(request.content()))));
                            } else {
                                send(ctx, request, HttpResponseStatus.METHOD_NOT_ALLOWED);
                            }
                        } else { // route.length == 4, get resource from document
                            // does the document have this resource?
                            long resourceId;
                            try {
                                resourceId = Long.parseLong(route.get(3));
                            } catch (NumberFormatException e) {
                                throw new BadRequestException("Exception while parsing " + route.get(3), e);
                            }

                            Document document = database.getDocument(documentId);
                            if (document == null)
                                throw new NotFoundException("Document with id " + documentId + " for user " + user + " somehow does not exist");

                            if (request.method().equals(HttpMethod.GET)) {
                                // get resource, resource must exist on the document
                                Resource resource = database.getResource(resourceId, documentId);
                                if (resource == null) {
                                    throw new NotFoundException("Resource does not exist");
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
                    default:
                        throw new NotFoundException("Bad route");
                }
            }

        }

        //login

        if (request.method().equals(HttpMethod.POST)) {
            //todo validate for extra fields that should not happen
            switch (route.path()) {
                case LOGIN_PATH: {
                    Form form = new Form(request);

                    String username = form.getText("username");
                    String password = form.getText("password");
                    boolean remember = form.getCheckbox("remember");


                    System.out.println(username + ", " + password + ", " + remember);

                    Login login = database.getLogin(username);
                    if (Login.verify(login, password.getBytes(StandardCharsets.UTF_8))) {
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
                    HttpUserSession userSession = new HttpUserSession(config, database.getUser(login.getId()), httpHeaders);
                    try {
                        database.addHttpSession(userSession);
                    } catch (FailedException e) {
                        throw new InternalServerException("Exception while adding http session to database", e);
                    }
                    if (remember) {
                        PersistentHttpUserSession persistentHttpUserSession = new PersistentHttpUserSession(config, userSession, httpHeaders);
                        try {
                            database.addPersistentHttpUserSession(persistentHttpUserSession);
                        } catch (FailedException e) {
                            throw new InternalServerException("Exception while adding persistent http session to database", e);
                        }
                    }
                    sendRedirect(ctx, request, httpHeaders, LOGIN_SUCCESS);
                    return;
                }
                case REGISTER_PATH: {
                    Form form = new Form(request);

                    // validate

                    String username = form.getText("username");
                    String password = form.getText("password");

                    //todo validate
                    if (false) {
                        //todo rate limit and generic error handling

                        sendRedirect(ctx, request, REGISTER_PAGE);
                        return;
                    }

                    User user;
                    HttpSessionCookie cookie = HttpUserSessionCookie.getHttpUserSessionCookie(request);
                    if (cookie != null) {
                        HttpUserSession httpSession = database.getHttpSession(cookie);
                        if (httpSession != null) {
                            User u = database.getUser(httpSession.getUser());
                            if (u.isRegistered()) {
                                Scribbleshare.getLogger(ctx).info("Registered user is creating a new account");
                                user = new User(username);
                                try {
                                    database.addUser(user);
                                } catch (FailedException e) {
                                    throw new InternalServerException("todo", e);
                                }
                            } else {
                                Scribbleshare.getLogger(ctx).info("Temporary user is registering");
                                user = u;
                            }
                        } else {
                            user = new User(username);
                            try {
                                database.addUser(user);
                            } catch (FailedException e) {
                                throw new InternalServerException("todo", e);
                            }
                        }
                    } else {
                        user = new User(username);
                        try {
                            database.addUser(user);
                        } catch (FailedException e) {
                            throw new InternalServerException("todo", e);
                        }
                    }

                    assert !user.isRegistered();

                    Login login = new Login(user, password.getBytes(StandardCharsets.UTF_8));
                    boolean loginAdded;
                    try {
                        loginAdded = database.addLogin(login);
                    } catch (FailedException e) {
                        throw new InternalServerException("Exception while adding login for user " + login.getId() + " with username " + login.getUsername(), e);
                    }
                    if (!loginAdded) {
                        Scribbleshare.getLogger(ctx).info("Tried to register with duplicate username " + username);
                        sendRedirect(ctx, request, REGISTER_PAGE);
                        return;
                    }

                    Scribbleshare.getLogger(ctx).info("Registered with username " + username);

                    sendRedirect(ctx, request, REGISTER_SUCCESS);
                    return;
                }
                case LOGOUT_PATH: {
                    Form form = new Form(request);//todo necessary?


                    HttpHeaders headers = new DefaultHttpHeaders();
                    HttpSessionCookie cookie = HttpUserSessionCookie.getHttpUserSessionCookie(request);
/*                if (cookie != null) {
                    HttpUserSession httpUserSession = database.getHttpSession(cookie);
                    if (httpUserSession != null) {
                        if (httpUserSession.validate(cookie)) {
                            httpUserSession.clearCookie(config, headers);
                            try {
                                database.expireHttpSession(httpUserSession);
                            } catch (FailedException e) {
                                //todo still send clear cookie
                                throw new InternalServerException("Failed to log out user", e);
                            }
                        } else {
                            Scribbleshare.getLogger(ctx).warning("Tried to log out of existing session with bad authentication");
                        }
                    } else {
                        Scribbleshare.getLogger(ctx).warning("Tried to log out of non existent session");
                    }
                }

                PersistentHttpUserSessionCookie persistentCookie = PersistentHttpUserSessionCookie.getHttpUserSessionCookie(request);
                if (persistentCookie != null) {
                    PersistentHttpUserSession persistentHttpUserSession = database.getPersistentHttpUserSession(persistentCookie);
                    if (persistentHttpUserSession != null) {
                        if (persistentHttpUserSession.validate(persistentCookie)) {
                            persistentCookie.clearCookie(config, headers);
                            try {
                                database.expirePersistentHttpUserSession(persistentHttpUserSession);
                            } catch (FailedException e) {
                                //todo still send clear cookie
                                throw new InternalServerException("Failed to log out user", e);
                            }
                        } else {
                            Scribbleshare.getLogger(ctx).warning("Tried to log out of existing persistent session with bad authentication");
                            //todo error
                        }
                    } else {
                        Scribbleshare.getLogger(ctx).warning("Tried to log out of non existent persistent session");
                        //todo error
                    }
                }*/

                    sendRedirect(ctx, request, headers, LOGOUT_SUCCESS);
                    return;
                }
            }
        }


        // otherwise try to serve a regular HTTP file resource

        if (!HttpMethod.GET.equals(request.method())) {
            send(ctx, request, HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }

        // redirects
        if (route.path().endsWith(DEFAULT_FILE)) { // /index.html -> /
            sendRedirect(ctx, request, route.path().substring(0, route.path().length() - DEFAULT_FILE.length()) + route.rawQuery());
            return;
        } else if ((route.path() + DEFAULT_FILE_EXTENSION).endsWith(DEFAULT_FILE)) { // /index -> /
            sendRedirect(ctx, request, route.path().substring(0, route.path().length() - (DEFAULT_FILE.length() - DEFAULT_FILE_EXTENSION.length())) + route.rawQuery());
            return;
        } else if (route.path().endsWith(DEFAULT_FILE_EXTENSION)) { // /page.html -> /page
            sendRedirect(ctx, request, route.path().substring(0, route.path().length() - DEFAULT_FILE_EXTENSION.length()) + route.rawQuery());
            return;
        }

        // get filesystem filePath from provided filePath
        final String filePath;
        try {
            filePath = getFilePath(route.path());
        } catch (BadRequestException e) {
            throw new NotFoundException("Exception while getting file path for http request", e);
        }

        //todo this is mostly for debug
        File root;
        if (filePath.endsWith(".js")) {
            root = jsRoot;
        } else {
            root = httpRoot;
        }

        File file = new File(root, filePath);
        if (file.isHidden() || !file.exists() || file.isDirectory() || !file.isFile()) {
            if (new File(httpRoot, filePath.substring(0, filePath.length() - DEFAULT_FILE_EXTENSION.length())).isDirectory()) { // /test -> /test/ if test is a valid directory and /test.html does not exist
                sendRedirect(ctx, request, route.path() + "/" + route.rawQuery());
            } else {
                throw new NotFoundException("File at path " + filePath + " not found");
            }
            return;
        }

        HttpHeaders headers = new DefaultHttpHeaders();
        /*if (route.equals(AUTHENTICATE_PAGE)) {
            try {
                logIn(config, request, headers);
            } catch (FailedException e) {
                throw new InternalServerException("Exception while logging user in", e);
            }
        }*/
        headers.set(HttpHeaderNames.CACHE_CONTROL, "public,max-age=" + httpCacheSeconds);//cache but revalidate if stale todo set to private cache for resources behind authentication
        try {
            sendFile(ctx, request, headers, file, mimeTypes.getMimeType(file));
        } catch (IOException e) {
            throw new InternalServerException("Exception while sending file", e);
        }
    }



    private static final Pattern ALLOWED_PATH = Pattern.compile("^[\\\\" + File.separator + "." + Uri.FILE_NAME_REGEX + "]+$");

    /** Converts uri to filesystem path */
    private static String getFilePath(String path) throws BadRequestException {
        path = path.replace("/", File.separator);

        if (path.contains(File.separator + '.') // /.
                || path.contains('.' + File.separator) // ./
                || path.contains(File.separator + File.separator) // //
                || path.charAt(0) == '.' // .
                || path.charAt(path.length() - 1) == '.' // /page.
                || !ALLOWED_PATH.matcher(path).matches())
            throw new BadRequestException("File path contains illegal characters");

        if (path.endsWith(File.separator)) { // / -> index.html
            path = path + DEFAULT_FILE;
        } else if (path.lastIndexOf(File.separator) > path.lastIndexOf(".")) { // /page -> /page.html
            path = path + DEFAULT_FILE_EXTENSION;
        }
        return path;
    }



}