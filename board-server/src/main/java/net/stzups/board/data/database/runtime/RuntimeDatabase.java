package net.stzups.board.data.database.runtime;

import net.stzups.board.Board;
import net.stzups.board.data.database.Database;
import net.stzups.board.data.objects.Document;
import net.stzups.board.data.objects.HttpSession;
import net.stzups.board.data.objects.User;
import net.stzups.board.data.objects.UserSession;

import java.util.HashMap;
import java.util.Map;

/**
 * In memory database, for debug only
 */
public class RuntimeDatabase implements Database {
    private Map<Long, Document> documents = new HashMap<>();
    private Map<Long, HttpSession> httpSessions = new HashMap<>();
    private Map<Long, UserSession> userSessions = new HashMap<>();
    private Map<Long, User> users = new HashMap<>();

    @Override
    public void addUser(User user) {
        users.put(user.getId(), user);
    }

    @Override
    public User getUser(long id) {
        return users.get(id);
    }

    @Override
    public Document getDocument(long id) {
        return documents.get(id);
    }

    @Override
    public UserSession removeUserSession(long token) {
        return userSessions.remove(token);
    }

    @Override
    public void addUserSession(UserSession userSession) {
        userSessions.put(userSession.getToken(), userSession);
    }

    @Override
    public HttpSession getHttpSession(long token) {
        return httpSessions.get(token);
    }

    @Override
    public void addHttpSession(HttpSession httpSession) {
        httpSessions.put(httpSession.getToken(), httpSession);
    }

    @Override
    public Document createDocument(User owner) {
        Document document = new Document(Board.getSecureRandom().nextLong(), owner, DEFAULT_DOCUMENT_NAME);
        documents.put(document.getId(), document);
        return document;
    }
}
