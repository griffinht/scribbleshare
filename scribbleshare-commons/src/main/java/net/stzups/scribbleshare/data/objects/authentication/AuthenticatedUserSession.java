package net.stzups.scribbleshare.data.objects.authentication;

public class AuthenticatedUserSession {
    private final long userId;

    public AuthenticatedUserSession(long userId) {
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }
}
