package net.stzups.board.data.database;

import net.stzups.board.data.objects.Document;
import net.stzups.board.data.objects.InviteCode;
import net.stzups.board.data.objects.User;
import net.stzups.board.data.objects.PersistentUserSession;
import net.stzups.board.data.objects.canvas.Canvas;

public interface Database {
    User createUser();
    User getUser(long id);
    void updateUser(User user);

    Document createDocument(User owner);
    Document getDocument(long id);
    void updateDocument(Document document);
    void deleteDocument(Document document);
    Canvas getCanvas(Document document);
    void saveCanvas(Canvas canvas);

    InviteCode getInviteCode(String code);
    InviteCode getInviteCode(Document document);

    PersistentUserSession removeUserSession(long id);
    void addUserSession(PersistentUserSession persistentUserSession);
}
