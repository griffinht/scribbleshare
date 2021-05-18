package net.stzups.scribbleshare.data.database.databases;

import net.stzups.scribbleshare.data.objects.authentication.http.HttpSessionCookie;
import net.stzups.scribbleshare.data.objects.authentication.http.PersistentHttpUserSession;

import java.sql.SQLException;

public interface PersistentHttpSessionDatabase {
    /**
     * Get existing {@link PersistentHttpUserSession}, or null if it does exist
     */
    PersistentHttpUserSession getPersistentHttpUserSession(HttpSessionCookie cookie);

    /**
     * Add new {@link PersistentHttpUserSession}
     */
    void addPersistentHttpUserSession(PersistentHttpUserSession persistentHttpSession) throws SQLException;

    /**
     * Expire existing {@link PersistentHttpUserSession}
     */
    void expirePersistentHttpUserSession(PersistentHttpUserSession persistentHttpUserSession) throws SQLException;
}
