package net.stzups.scribbleshare.data.database.databases;

import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpSessionCookie;
import net.stzups.scribbleshare.data.objects.authentication.http.PersistentHttpUserSession;

public interface PersistentHttpSessionDatabase {
    /**
     * Get existing {@link PersistentHttpUserSession}, or null if it does exist
     */
    PersistentHttpUserSession getPersistentHttpUserSession(HttpSessionCookie cookie);

    /**
     * Add new {@link PersistentHttpUserSession}
     */
    void addPersistentHttpUserSession(PersistentHttpUserSession persistentHttpSession) throws DatabaseException;

    /**
     * Expire existing {@link PersistentHttpUserSession}
     */
    void expirePersistentHttpUserSession(PersistentHttpUserSession persistentHttpUserSession) throws DatabaseException;
}
