package net.stzups.scribbleshare.data.database.databases;

import net.stzups.scribbleshare.data.database.exception.exceptions.FailedException;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpSessionCookie;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSession;

public interface HttpSessionDatabase {
    /**
     * Get existing {@link HttpUserSession}, or null if it does not exist
     */
    HttpUserSession getHttpSession(HttpSessionCookie cookie);

    /**
     * Add new {@link HttpUserSession}
     */
    void addHttpSession(HttpUserSession httpUserSession) throws FailedException;

    /**
     * Expire existing {@link HttpUserSession}
     */
    void expireHttpSession(HttpUserSession httpUserSession) throws FailedException;
}
