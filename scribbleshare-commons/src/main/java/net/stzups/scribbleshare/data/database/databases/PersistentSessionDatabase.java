package net.stzups.scribbleshare.data.database.databases;

import net.stzups.scribbleshare.data.objects.authentication.http.PersistentHttpUserSession;

public interface PersistentSessionDatabase {
    PersistentHttpUserSession getAndRemovePersistentHttpSession(long id);
    void addPersistentHttpSession(PersistentHttpUserSession persistentHttpSession);
}
