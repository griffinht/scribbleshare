package net.stzups.scribbleshare.data.database.databases;

import net.stzups.scribbleshare.data.objects.authentication.http.PersistentHttpUserSession;

public interface PersistentSessionDatabase {
    PersistentHttpUserSession getAndExpirePersistentHttpSession(long id);
    void addPersistentHttpSession(PersistentHttpUserSession persistentHttpSession);
    void expirePersistentHttpSession(long id);
}
