package net.stzups.scribbleshare.data.database.databases;

import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpSessionCookie;
import net.stzups.scribbleshare.data.objects.authentication.http.PersistentHttpUserSession;
import org.jetbrains.annotations.Nullable;

public interface PersistentHttpSessionDatabase {
    /**
     * @param cookie {@link HttpSessionCookie} of {@link PersistentHttpUserSession}
     * @return null if {@link PersistentHttpUserSession} does not exist
     */
    @Nullable PersistentHttpUserSession getPersistentHttpUserSession(HttpSessionCookie cookie) throws DatabaseException;

    void addPersistentHttpUserSession(PersistentHttpUserSession persistentHttpSession) throws DatabaseException;

    /**
     * Expire existing {@link PersistentHttpUserSession}
     * todo fail silently or loudly if the persistent http user session does not exist?
     */
    void expirePersistentHttpUserSession(PersistentHttpUserSession persistentHttpUserSession) throws DatabaseException;
}
