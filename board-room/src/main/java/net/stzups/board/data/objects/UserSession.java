package net.stzups.board.data.objects;

import net.stzups.board.BoardRoom;

public class UserSession {
    private static final int MAX_USER_SESSION_AGE = 10000000;//todo
    private long token;
    private long userId;
    private long creationTime;
    private long hash;

    public UserSession(User user, long hash) {//todo hash
        this.token = BoardRoom.getSecureRandom().nextLong();//https://paragonie.com/blog/2015/04/secure-authentication-php-with-long-term-persistence
        this.userId = user.getId();
        this.creationTime = System.currentTimeMillis();//todo security issue? round/fuzz by a few seconds?
        this.hash = hash;
    }

    public UserSession(long token, long userId, long creationTime, long hash) {
        this.token = token;
        this.userId = userId;
        this.creationTime = creationTime;
        this.hash = hash;
    }

    public long getToken() {
        return token;
    }

    public long getUserId() {
        return userId;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getHash() {
        return hash;
    }

    public boolean validate(long hash) {
        token = 0;
        return (System.currentTimeMillis() - creationTime) < MAX_USER_SESSION_AGE && this.hash == hash;
    }

    @Override
    public String toString() {
        return "UserSession{userId=" + userId + ",@" + hashCode()+ "}";
    }

    @Override
    public int hashCode() {
        return Long.hashCode(userId);
    }
}
