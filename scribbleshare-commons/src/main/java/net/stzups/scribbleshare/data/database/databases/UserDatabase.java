package net.stzups.scribbleshare.data.database.databases;

import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.User;
import org.jetbrains.annotations.Nullable;

public interface UserDatabase {
    void addUser(User user) throws DatabaseException;

    /**
     * @return null if the {@link User} does not exist for {@param id}
     */
    @Nullable User getUser(long id);

    void updateUser(User user) throws DatabaseException;
}
