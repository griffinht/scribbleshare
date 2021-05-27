package net.stzups.scribbleshare.backend.server.http.handler.handlers;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.stream.ChunkedStream;
import net.stzups.scribbleshare.data.database.ScribbleshareDatabase;
import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.Resource;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.data.objects.authentication.AuthenticatedUserSession;
import net.stzups.scribbleshare.server.http.exception.HttpException;
import net.stzups.scribbleshare.server.http.exception.exceptions.BadRequestException;
import net.stzups.scribbleshare.server.http.exception.exceptions.InternalServerException;
import net.stzups.scribbleshare.server.http.exception.exceptions.NotFoundException;
import net.stzups.scribbleshare.server.http.exception.exceptions.UnauthorizedException;
import net.stzups.scribbleshare.server.http.handler.RequestHandler;
import net.stzups.scribbleshare.server.http.handler.handlers.HttpAuthenticator;
import net.stzups.scribbleshare.server.http.objects.Route;

import static net.stzups.scribbleshare.server.http.HttpUtils.send;
import static net.stzups.scribbleshare.server.http.HttpUtils.sendChunkedResource;

public class DocumentRequestHandler extends RequestHandler {
    private static final long MAX_AGE_NO_EXPIRE = 31536000;//one year, max age of a cookie

    private final ScribbleshareDatabase database;

    public DocumentRequestHandler(ScribbleshareDatabase database) {
        super("/document");
        this.database = database;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest request, Route route) throws HttpException {
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
                Document document;
                try {
                    document = database.getDocument(documentId);
                } catch (DatabaseException e) {
                    throw new InternalServerException(e);
                }
                if (document == null)
                    throw new NotFoundException("Document with id " + documentId + " for user " + user + " somehow does not exist");

                try {
                    send(ctx, request, Unpooled.copyLong(database.addResource(document.getId(), new Resource(request.content()))));
                } catch (DatabaseException e) {
                    throw new InternalServerException(e);
                }
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

                HttpHeaders headers = new DefaultHttpHeaders();
                headers.add(HttpHeaderNames.CACHE_CONTROL, "private,max-age=" + MAX_AGE_NO_EXPIRE + ",immutable");//cache and never revalidate - permanent
                sendChunkedResource(ctx, request, headers, new ChunkedStream(new ByteBufInputStream(resource.getData())));
            } else {
                send(ctx, request, HttpResponseStatus.METHOD_NOT_ALLOWED);
            }
        }
    }
}
