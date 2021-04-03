package net.stzups.board.data.objects;

import io.netty.buffer.Unpooled;
import net.stzups.board.BoardRoom;

import java.util.Arrays;

public class PersistentUserSession {
    private static final int MAX_USER_SESSION_AGE = 10000000;//todo
    private long id;
    private long user;
    private long creationTime;
    private byte[] hashedToken;

    public PersistentUserSession(User user) {
        this.id = BoardRoom.getSecureRandom().nextLong();//todo secure random or regular random?
        this.user = user.getId();
        this.creationTime = System.currentTimeMillis();
    }

    public PersistentUserSession(long id, long user, long creationTime, byte[] hashedToken) {
        this.id = id;
        this.user = user;
        this.creationTime = creationTime;
        this.hashedToken = hashedToken;
    }

    /** should be called once after instance creation */
    public long generateToken() {
        long token = BoardRoom.getSecureRandom().nextLong();
        hashedToken = BoardRoom.getSHA256MessageDigest().digest(Unpooled.copyLong(token).array());
        return token;
    }

    public long getId() {
        return id;
    }

    public long getUser() {
        return user;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public byte[] getHashedToken() {
        return hashedToken;
    }

    public boolean validate(long token) {
        byte[] hashedToken = BoardRoom.getSHA256MessageDigest().digest(Unpooled.copyLong(token).array());
        boolean validate = (System.currentTimeMillis() - creationTime) < MAX_USER_SESSION_AGE && Arrays.equals(this.hashedToken, hashedToken);
        //this session will have already been deleted in db and should be garbage collected right after this, but just in case zero the hashes so it won't work again
        Arrays.fill(this.hashedToken, (byte) 0);
        return validate;
    }

    @Override
    public String toString() {
        return "UserSession{id=" + id + ",user" + user + ",creationTime=" + creationTime + "}";
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
