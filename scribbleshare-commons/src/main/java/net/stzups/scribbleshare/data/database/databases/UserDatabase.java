package net.stzups.scribbleshare.data.database.databases;

import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.User;

public interface UserDatabase {
    void addUser(User user) throws DatabaseException;
    User getUser(long id);
    void updateUser(User user) throws DatabaseException;
}
