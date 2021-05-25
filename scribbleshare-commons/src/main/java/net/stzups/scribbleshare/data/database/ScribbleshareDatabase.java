package net.stzups.scribbleshare.data.database;

import net.stzups.scribbleshare.data.database.databases.DocumentDatabase;
import net.stzups.scribbleshare.data.database.databases.HttpSessionDatabase;
import net.stzups.scribbleshare.data.database.databases.InviteCodeDatabase;
import net.stzups.scribbleshare.data.database.databases.LoginDatabase;
import net.stzups.scribbleshare.data.database.databases.PersistentHttpSessionDatabase;
import net.stzups.scribbleshare.data.database.databases.ResourceDatabase;
import net.stzups.scribbleshare.data.database.databases.UserDatabase;
import net.stzups.scribbleshare.server.http.handlers.HttpAuthenticator;

public interface ScribbleshareDatabase extends PersistentHttpSessionDatabase, ResourceDatabase, HttpSessionDatabase, UserDatabase, DocumentDatabase, InviteCodeDatabase, LoginDatabase,
        HttpAuthenticator.Database {

}
