package net.stzups.scribbleshare.data.database;

import net.stzups.scribbleshare.data.database.databases.PersistentSessionDatabase;
import net.stzups.scribbleshare.data.database.databases.ResourceDatabase;
import net.stzups.scribbleshare.data.database.databases.SessionDatabase;
import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.InviteCode;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.data.objects.authentication.login.Login;

public interface ScribbleshareDatabase extends PersistentSessionDatabase, ResourceDatabase, SessionDatabase {
    void addUser(User user);
    User getUser(long id);
    void updateUser(User user);

    Document createDocument(User owner);
    Document getDocument(long id);
    void updateDocument(Document document);
    void deleteDocument(Document document);

    InviteCode getInviteCode(String code);
    InviteCode getInviteCode(Document document);
    Login getLogin(String username);
    void addLogin(Login login);
}
