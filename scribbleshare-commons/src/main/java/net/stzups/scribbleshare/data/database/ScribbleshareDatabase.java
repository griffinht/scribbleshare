package net.stzups.scribbleshare.data.database;

import net.stzups.scribbleshare.data.database.databases.DocumentDatabase;
import net.stzups.scribbleshare.data.database.databases.HttpSessionDatabase;
import net.stzups.scribbleshare.data.database.databases.InviteCodeDatabase;
import net.stzups.scribbleshare.data.database.databases.LoginDatabase;
import net.stzups.scribbleshare.data.database.databases.ResourceDatabase;
import net.stzups.scribbleshare.data.database.databases.UserDatabase;

public interface ScribbleshareDatabase extends DocumentDatabase, HttpSessionDatabase, InviteCodeDatabase, LoginDatabase, ResourceDatabase, UserDatabase {
}
