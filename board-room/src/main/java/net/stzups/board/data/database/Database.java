package net.stzups.board.data.database;

import net.stzups.board.data.objects.Document;
import net.stzups.board.data.objects.HttpSession;
import net.stzups.board.data.objects.User;
import net.stzups.board.data.objects.UserSession;
import net.stzups.board.data.objects.canvas.Canvas;

public interface Database {
    void addUser(User user);
    User getUser(long id);

    Document createDocument(User owner);
    Document getDocument(long id);
    Canvas getCanvas(Document document);
    void saveCanvas(Canvas canvas);
    void saveDocument(Document document);

    UserSession removeUserSession(long token);
    void addUserSession(UserSession userSession);
}
