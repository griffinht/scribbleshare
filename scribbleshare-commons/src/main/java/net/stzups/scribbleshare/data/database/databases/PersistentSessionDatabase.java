package net.stzups.scribbleshare.data.database.databases;

import net.stzups.scribbleshare.data.objects.authentication.http.HttpSessionCookie;
import net.stzups.scribbleshare.data.objects.authentication.http.PersistentHttpUserSession;

import java.sql.SQLException;

public interface PersistentSessionDatabase {
    /**
     * Get existing {@link PersistentHttpUserSession} and expire it, or null if it does exist
     */
    PersistentHttpUserSession getAndExpirePersistentHttpSession(HttpSessionCookie cookie);

    /**
     * Add new {@link PersistentHttpUserSession}
     */
    void addPersistentHttpSession(PersistentHttpUserSession persistentHttpSession) throws SQLException;
}
