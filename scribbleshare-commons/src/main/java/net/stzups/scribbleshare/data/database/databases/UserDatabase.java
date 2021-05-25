package net.stzups.scribbleshare.data.database.databases;

import net.stzups.scribbleshare.data.database.exception.exceptions.FailedException;
import net.stzups.scribbleshare.data.objects.User;

public interface UserDatabase {
    void addUser(User user) throws FailedException;
    User getUser(long id);
    void updateUser(User user) throws FailedException;
}
