package net.stzups.scribbleshare.backend.server.http;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.backend.server.http.handler.HttpHandler;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpConfig;
import net.stzups.scribbleshare.server.http.exception.HttpException;
import net.stzups.scribbleshare.server.http.exception.exceptions.BadRequestException;
import net.stzups.scribbleshare.server.http.exception.exceptions.InternalServerException;
import net.stzups.scribbleshare.server.http.exception.exceptions.NotFoundException;
import net.stzups.scribbleshare.server.http.objects.Route;
import net.stzups.scribbleshare.server.http.objects.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;

import static net.stzups.scribbleshare.server.http.HttpUtils.send;
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



    private static final String DEFAULT_FILE = "index.html"; // / -> /index.html
    private static final String DEFAULT_FILE_EXTENSION = ".html"; // /index -> index.html






    private static final String AUTHENTICATE_PAGE = "/"; // the page where new users will be automatically created








    private final File jsRoot;
    private final File httpRoot;
    private final int httpCacheSeconds;
    private final MimeTypes mimeTypes = new MimeTypes();

    private final Map<String, HttpHandler> handlers = new HashMap<>();

    public HttpServerHandler(Config config) {
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

    public HttpServerHandler addHandler(HttpHandler handler) {
        handlers.put(handler.getRoute(), handler);
        return this;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
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

        HttpHandler handler = handlers.get(route.path());
        if (handler != null) {
            handler.handle(ctx, request);
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