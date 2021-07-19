package net.stzups.scribbleshare.backend.data.database;

import net.stzups.scribbleshare.backend.data.database.databases.PersistentHttpSessionDatabase;
import net.stzups.scribbleshare.data.database.ScribbleshareDatabase;

public interface ScribbleshareBackendDatabase extends ScribbleshareDatabase, PersistentHttpSessionDatabase {
}
