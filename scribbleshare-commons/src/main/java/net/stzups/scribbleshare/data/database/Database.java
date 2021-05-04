package net.stzups.scribbleshare.data.database;

import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.InviteCode;
import net.stzups.scribbleshare.data.objects.Resource;
import net.stzups.scribbleshare.data.objects.session.HttpSession;
import net.stzups.scribbleshare.data.objects.session.PersistentHttpSession;
import net.stzups.scribbleshare.data.objects.User;

public interface Database extends AutoCloseable {
    User createUser();
    User getUser(long id);
    void updateUser(User user);

    Document createDocument(User owner);
    Document getDocument(long id);
    void updateDocument(Document document);
    void deleteDocument(Document document);

    InviteCode getInviteCode(String code);
    InviteCode getInviteCode(Document document);

    PersistentHttpSession getAndRemovePersistentHttpSession(long id);
    void addPersistentHttpSession(PersistentHttpSession persistentHttpSession);

    HttpSession getHttpSession(long id);
    void addHttpSession(HttpSession httpSession);

    /**
     * Add resource to database and return the corresponding id for the new resource
     */
    long addResource(long owner, Resource resource);

    /** update resource */
    void updateResource(long id, long owner, Resource resource);

    /**
     * Gets resource, or null if the resource does not exist
     */
    Resource getResource(long id, long owner);
}
