package net.stzups.board.data.database;

import net.stzups.board.data.objects.Document;
import net.stzups.board.data.objects.HttpSession;
import net.stzups.board.data.objects.User;
import net.stzups.board.data.objects.UserSession;

public interface Database {
    void addUser(User user);
    User getUser(long id);
    Document getDocument(long id);
    UserSession removeUserSession(long token);
    void addUserSession(UserSession userSession);
    Document createDocument(User owner);
}
