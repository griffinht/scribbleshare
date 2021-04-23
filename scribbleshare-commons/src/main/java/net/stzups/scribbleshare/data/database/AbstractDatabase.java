package net.stzups.scribbleshare.data.database;

import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.InviteCode;
import net.stzups.scribbleshare.data.objects.Resource;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.data.objects.session.HttpSession;
import net.stzups.scribbleshare.data.objects.session.PersistentHttpSession;

public class AbstractDatabase implements Database {
    @Override
    public User createUser() {
        throw new UnsupportedOperationException();
    }

    @Override
    public User getUser(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateUser(User user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Document createDocument(User owner) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Document getDocument(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateDocument(Document document) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteDocument(Document document) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InviteCode getInviteCode(String code) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InviteCode getInviteCode(Document document) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PersistentHttpSession getAndRemovePersistentHttpSession(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addPersistentHttpSession(PersistentHttpSession persistentHttpSession) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpSession getHttpSession(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addHttpSession(HttpSession httpSession) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long addResource(long owner, Resource resource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateResource(long id, long owner, Resource resource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Resource getResource(long id, long owner) {
        throw new UnsupportedOperationException();
    }
}
