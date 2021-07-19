package net.stzups.scribbleshare.data.objects.authentication;

import net.stzups.scribbleshare.data.objects.User;
import net.stzups.util.DebugString;

public class AuthenticatedUserSession {
    private final User user;

    public AuthenticatedUserSession(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return DebugString.get(AuthenticatedUserSession.class)
                .add("user", user)
                .toString();
    }
}
