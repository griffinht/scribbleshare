package net.stzups.scribbleshare.data.database.databases;

import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.User;
import org.jetbrains.annotations.Nullable;

public interface UserDatabase {
    void addUser(User user) throws DatabaseException;

    /**
     * @param id id of {@link User}
     * @return null if the {@link User} does not exist
     */
    @Nullable User getUser(long id) throws DatabaseException;

    void updateUser(User user) throws DatabaseException;
}
