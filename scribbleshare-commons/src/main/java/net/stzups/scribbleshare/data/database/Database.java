package net.stzups.scribbleshare.data.database;

import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.InviteCode;
import net.stzups.scribbleshare.data.objects.session.HttpSession;
import net.stzups.scribbleshare.data.objects.session.PersistentHttpSession;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.data.objects.canvas.Canvas;

public interface Database {
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
    long addResource(byte[] resource);

    /**
     * Update resource with id, returns false if the resource does not exist
     */
    boolean updateResource(long id, byte[] resource);

    /**
     * Gets resource, or null if the resource does not exist
     */
    byte[] getResource(long id);
}
