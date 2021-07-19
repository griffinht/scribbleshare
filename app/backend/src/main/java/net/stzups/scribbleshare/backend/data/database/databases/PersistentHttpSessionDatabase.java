package net.stzups.scribbleshare.backend.data.database.databases;

import net.stzups.scribbleshare.backend.data.PersistentHttpUserSession;
import net.stzups.scribbleshare.backend.data.PersistentHttpUserSessionCookie;
import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpSessionCookie;
import org.jetbrains.annotations.Nullable;

public interface PersistentHttpSessionDatabase {
    /**
     * @param cookie {@link HttpSessionCookie} of {@link PersistentHttpUserSession}
     * @return null if {@link PersistentHttpUserSession} does not exist
     */
    @Nullable
    PersistentHttpUserSession getPersistentHttpUserSession(PersistentHttpUserSessionCookie cookie) throws DatabaseException;

    void addPersistentHttpUserSession(PersistentHttpUserSession persistentHttpSession) throws DatabaseException;

    /**
     * Expire existing {@link PersistentHttpUserSession}
     * todo fail silently or loudly if the persistent http user session does not exist?
     */
    void expirePersistentHttpUserSession(PersistentHttpUserSession persistentHttpUserSession) throws DatabaseException;
}
