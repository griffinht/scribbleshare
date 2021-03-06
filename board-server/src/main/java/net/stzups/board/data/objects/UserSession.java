package net.stzups.board.data.objects;

import net.stzups.board.Board;

import java.io.Serializable;

public class UserSession implements Serializable {
    private static final int MAX_USER_SESSION_AGE = 10000000;//todo
    private long userId;
    private long token;
    private long creation_time;
    private long hash;

    public UserSession(User user, long hash) {//todo hash
        this.userId = user.getId();
        this.token = Board.getSecureRandom().nextLong();
        this.creation_time = System.currentTimeMillis();//todo security issue? round/fuzz by a few seconds?
        this.hash = hash;
    }

    public long getToken() {
        return token;
    }

    public long getUserId() {
        return userId;
    }

    public boolean validate(long hash) {
        token = 0;
        return (System.currentTimeMillis() - creation_time) < MAX_USER_SESSION_AGE && this.hash == hash;
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
