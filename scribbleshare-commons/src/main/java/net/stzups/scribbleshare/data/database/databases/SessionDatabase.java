package net.stzups.scribbleshare.data.database.databases;

import net.stzups.scribbleshare.data.objects.session.HttpSession;

public interface SessionDatabase {


    HttpSession getHttpSession(long id);
    void addHttpSession(HttpSession httpSession);
}
