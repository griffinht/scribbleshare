package net.stzups.board.data.database.memory;

import net.stzups.board.BoardRoom;
import net.stzups.board.data.database.Database;
import net.stzups.board.data.objects.Document;
import net.stzups.board.data.objects.InviteCode;
import net.stzups.board.data.objects.User;
import net.stzups.board.data.objects.PersistentUserSession;
import net.stzups.board.data.objects.canvas.Canvas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * In memory database, for debug only
 */
public class MemoryDatabase implements Database {
    private Map<Long, Document> documents = new HashMap<>();
    private Map<Long, PersistentUserSession> userSessions = new HashMap<>();
    private Map<Long, User> users = new HashMap<>();

    @Override
    public User createUser() {
        User user = new User(BoardRoom.getRandom().nextLong(), new Long[0], new Long[0]);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User getUser(long id) {
        return users.get(id);
    }

    @Override
    public void updateUser(User user) {

    }

    @Override
    public Document getDocument(long id) {
        return documents.get(id);
    }

    @Override
    public Canvas getCanvas(Document document) {
        return new Canvas(document);
    }

    @Override
    public void saveCanvas(Canvas canvas) {

    }

    @Override
    public InviteCode getInviteCode(String code) {
        return null;
    }

    @Override
    public InviteCode getInviteCode(Document document) {
        return null;
    }

    @Override
    public void updateDocument(Document document) {
        documents.put(document.getId(), document);
    }

    @Override
    public void deleteDocument(Document document) {
        documents.remove(document.getId());
    }

    @Override
    public PersistentUserSession removeUserSession(long id) {
        return userSessions.remove(id);
    }

    @Override
    public void addUserSession(PersistentUserSession persistentUserSession) {
        userSessions.put(persistentUserSession.getId(), persistentUserSession);
    }

    @Override
    public Document createDocument(User owner) {
        Document document = new Document(owner);
        documents.put(document.getId(), document);
        return document;
    }
}
