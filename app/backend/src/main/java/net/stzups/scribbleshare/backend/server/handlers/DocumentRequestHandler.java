package net.stzups.scribbleshare.backend.server.handlers;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.stream.ChunkedStream;
import net.stzups.netty.http.exception.HttpException;
import net.stzups.netty.http.exception.exceptions.BadRequestException;
import net.stzups.netty.http.exception.exceptions.InternalServerException;
import net.stzups.netty.http.exception.exceptions.MethodNotAllowedException;
import net.stzups.netty.http.exception.exceptions.NotFoundException;
import net.stzups.netty.http.exception.exceptions.UnauthorizedException;
import net.stzups.netty.http.handler.RequestHandler;
import net.stzups.netty.http.objects.Route;
import net.stzups.scribbleshare.data.database.databases.DocumentDatabase;
import net.stzups.scribbleshare.data.database.databases.HttpSessionDatabase;
import net.stzups.scribbleshare.data.database.databases.ResourceDatabase;
import net.stzups.scribbleshare.data.database.databases.UserDatabase;
import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.Resource;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.data.objects.authentication.AuthenticatedUserSession;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpConfig;
import net.stzups.scribbleshare.server.http.handler.handlers.HttpAuthenticator;

import static net.stzups.netty.http.HttpUtils.send;

public class DocumentRequestHandler<T extends ResourceDatabase & DocumentDatabase & HttpSessionDatabase & UserDatabase> extends RequestHandler {
    private static final long MAX_AGE_NO_EXPIRE = 31536000;//one year, max age of a cookie

    private final T database;

    public DocumentRequestHandler(HttpConfig config, T database) {
        super(config, "/", "/document");
        this.database = database;
    }

    @Override
    public void handleRequest(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponse response) throws HttpException {
        Route route = new Route(request.uri());
        AuthenticatedUserSession session = HttpAuthenticator.authenticateHttpUserSession(database, request);

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
                Document document;
                try {
                    document = database.getDocument(documentId);
                } catch (DatabaseException e) {
                    throw new InternalServerException(e);
                }
                if (document == null)
                    throw new NotFoundException("Document with id " + documentId + " for user " + user + " somehow does not exist");

                try {
                    send(ctx, request, response, Unpooled.copyLong(database.addResource(document.getId(), new Resource(request.content()))));
                } catch (DatabaseException e) {
                    throw new InternalServerException(e);
                }
            } else {
                throw new MethodNotAllowedException(request.method());
            }
        } else { // route.length == 4, get resource from document
            // does the document have this resource?
            long resourceId;
            try {
                resourceId = Long.parseLong(route.get(3));
            } catch (NumberFormatException e) {
                throw new BadRequestException("Exception while parsing " + route.get(3), e);
            }

            Document document;
            try {
                document = database.getDocument(documentId);
            } catch (DatabaseException e) {
                throw new InternalServerException(e);
            }
            if (document == null)
                throw new NotFoundException("Document with id " + documentId + " for user " + user + " somehow does not exist");

            if (request.method().equals(HttpMethod.GET)) {
                // get resource, resource must exist on the document
                Resource resource;
                try {
                    resource = database.getResource(resourceId, documentId);
                } catch (DatabaseException e) {
                    throw new InternalServerException(e);
                }
                if (resource == null) {
                    throw new NotFoundException("Resource does not exist");
                }

                response.headers().add(HttpHeaderNames.CACHE_CONTROL, "private,max-age=" + MAX_AGE_NO_EXPIRE + ",immutable");//cache and never revalidate - permanent
                send(ctx, request, response, new HttpChunkedInput(new ChunkedStream(new ByteBufInputStream(resource.getData()))));
            } else {
                throw new MethodNotAllowedException(request.method());
            }
        }
    }
}
