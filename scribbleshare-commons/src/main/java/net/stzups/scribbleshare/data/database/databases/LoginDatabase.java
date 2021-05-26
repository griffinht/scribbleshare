package net.stzups.scribbleshare.data.database.databases;

import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.authentication.login.Login;
import org.jetbrains.annotations.Nullable;

public interface LoginDatabase {
    /**
     * @param username username of {@link Login}
     * @return null if {@link Login} does not exist
     */
    @Nullable Login getLogin(String username);

    /**
     * @return false if the login with duplicate username already exists
     */
    boolean addLogin(Login login) throws DatabaseException;
}
