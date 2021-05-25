package net.stzups.scribbleshare.data.objects.authentication;

import net.stzups.scribbleshare.data.objects.User;

public class AuthenticatedUserSession {
    private final User user;

    public AuthenticatedUserSession(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
