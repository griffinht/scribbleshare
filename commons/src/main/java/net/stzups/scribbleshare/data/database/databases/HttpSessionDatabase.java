package net.stzups.scribbleshare.data.database.databases;

import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSession;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSessionCookie;
import org.jetbrains.annotations.Nullable;

public interface HttpSessionDatabase {
    /**
     * @param cookie cookie of {@link HttpUserSession}
     * @return null if the {@link HttpUserSession} does not exist
     */
    @Nullable HttpUserSession getHttpSession(HttpUserSessionCookie cookie) throws DatabaseException;

    void addHttpSession(HttpUserSession httpUserSession) throws DatabaseException;

    /**
     * Expire existing {@link HttpUserSession}
     * todo fail if does not exist?
     */
    void expireHttpSession(HttpUserSession httpUserSession) throws DatabaseException;
}
