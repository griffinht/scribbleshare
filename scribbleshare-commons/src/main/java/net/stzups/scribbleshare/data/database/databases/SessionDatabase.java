package net.stzups.scribbleshare.data.database.databases;

import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSession;

public interface SessionDatabase {


    HttpUserSession getHttpSession(long id);
    void addHttpSession(HttpUserSession httpSession);
    void removeHttpSession(long id);
}
