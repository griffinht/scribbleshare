package net.stzups.scribbleshare.data.database.databases;

import net.stzups.scribbleshare.data.database.exception.exceptions.FailedException;
import net.stzups.scribbleshare.data.objects.authentication.login.Login;

public interface LoginDatabase {
    Login getLogin(String username);
    /** false if the username already existed */
    boolean addLogin(Login login) throws FailedException;
}
