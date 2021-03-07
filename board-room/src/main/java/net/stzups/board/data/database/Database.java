package net.stzups.board.data.database;

import net.stzups.board.data.objects.Document;
import net.stzups.board.data.objects.HttpSession;
import net.stzups.board.data.objects.User;
import net.stzups.board.data.objects.UserSession;

public interface Database {
    String DEFAULT_DOCUMENT_NAME = "Untitled Document";

    void addUser(User user);
    User getUser(long id);
    Document getDocument(long id);
    UserSession removeUserSession(long token);
    void addUserSession(UserSession userSession);
    HttpSession getHttpSession(long token);
    void addHttpSession(HttpSession httpSession);
    Document createDocument(User owner);
}
