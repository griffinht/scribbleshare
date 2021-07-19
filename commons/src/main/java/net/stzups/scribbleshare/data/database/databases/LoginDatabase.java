package net.stzups.scribbleshare.data.database.databases;

import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.authentication.login.Login;
import org.jetbrains.annotations.Nullable;

public interface LoginDatabase {
    /**
     * It is important when verifying logins that the operation takes the same amount of time regardless of whether a {@link Login} exists for a provided username, or the result of the verification of the login.
     * @param username username of {@link Login}
     * @return null if {@link Login} does not exist
     */
    @Nullable Login getLogin(String username) throws DatabaseException;

    /**
     * @return false if the login with duplicate username already exists
     */
    boolean addLogin(Login login) throws DatabaseException;
}
