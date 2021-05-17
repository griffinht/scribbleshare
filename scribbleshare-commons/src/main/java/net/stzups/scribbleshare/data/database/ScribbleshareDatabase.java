package net.stzups.scribbleshare.data.database;

import net.stzups.scribbleshare.data.database.databases.MiscDatabase;
import net.stzups.scribbleshare.data.database.databases.PersistentSessionDatabase;
import net.stzups.scribbleshare.data.database.databases.ResourceDatabase;
import net.stzups.scribbleshare.data.database.databases.SessionDatabase;
import net.stzups.scribbleshare.data.objects.authentication.login.Login;

public interface ScribbleshareDatabase extends MiscDatabase, PersistentSessionDatabase, ResourceDatabase, SessionDatabase {
    Login getLogin(String username);
}
