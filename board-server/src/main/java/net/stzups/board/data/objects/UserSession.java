package net.stzups.board.data.objects;

import net.stzups.board.data.TokenGenerator;

import java.io.Serializable;
import java.net.InetAddress;

public class UserSession implements Serializable {
    private static final int MAX_USER_SESSION_AGE = 10000000;//todo
    private long userId;
    private long token;
    private long creationDate;
    private InetAddress inetAddress;//todo hash?

    public UserSession(User user, InetAddress inetAddress) {
        this.userId = user.getId();
        this.token = TokenGenerator.getSecureRandom().nextLong();
        this.creationDate = System.currentTimeMillis();
        this.inetAddress = inetAddress;
    }

    public long getToken() {
        return token;
    }

    public long getUserId() {
        return userId;
    }

    /** User session should be invalidated if this returns false */
    public boolean validate(InetAddress inetAddress) {
        return (System.currentTimeMillis() - creationDate) < MAX_USER_SESSION_AGE && this.inetAddress.equals(inetAddress);
    }

    @Override
    public String toString() {
        return "UserSession{userId=" + userId + ",token=" + token+ "}";
    }
}
