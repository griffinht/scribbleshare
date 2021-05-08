package net.stzups.scribbleshare.data.database.databases;

import net.stzups.scribbleshare.data.objects.session.PersistentHttpSession;

public interface PersistentSessionDatabase {
    PersistentHttpSession getAndRemovePersistentHttpSession(long id);
    void addPersistentHttpSession(PersistentHttpSession persistentHttpSession);
}
