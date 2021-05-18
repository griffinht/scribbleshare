package net.stzups.scribbleshare.data.database.databases;

import net.stzups.scribbleshare.data.objects.authentication.http.PersistentHttpUserSession;

import java.sql.SQLException;

public interface PersistentSessionDatabase {
    /**
     * Get existing {@link PersistentHttpUserSession} and expire it, or null if it does exist
     */
    PersistentHttpUserSession getAndExpirePersistentHttpSession(long id);

    /**
     * Add new {@link PersistentHttpUserSession}
     */
    void addPersistentHttpSession(PersistentHttpUserSession persistentHttpSession) throws SQLException;

    /**
     * Expire existing {@link PersistentHttpUserSession}
     */
    void expirePersistentHttpSession(PersistentHttpUserSession persistentHttpSession) throws SQLException;
}
