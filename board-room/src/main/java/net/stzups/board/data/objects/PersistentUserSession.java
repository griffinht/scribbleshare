package net.stzups.board.data.objects;

import io.netty.buffer.Unpooled;
import net.stzups.board.BoardRoom;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;

public class PersistentUserSession {
    private static final TemporalAmount MAX_USER_SESSION_AGE = Duration.ofDays(90);//todo
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final MessageDigest messageDigest;
    static {
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private long id;
    private long user;
    private Timestamp creation;
    private byte[] hashedToken;

    public PersistentUserSession(User user) {
        this.id = BoardRoom.getRandom().nextLong();//todo secure random or regular random?
        this.user = user.getId();
        this.creation = new Timestamp(Instant.now().toEpochMilli());
    }

    public PersistentUserSession(long id, long user, Timestamp creation, byte[] hashedToken) {
        this.id = id;
        this.user = user;
        this.creation = creation;
        this.hashedToken = hashedToken;
    }

    /** should be called once after instance creation */
    public long generateToken() {
        long token = secureRandom.nextLong();
        hashedToken = messageDigest.digest(Unpooled.copyLong(token).array());
        return token;
    }

    public long getId() {
        return id;
    }

    public long getUser() {
        return user;
    }

    public Timestamp getCreation() {
        return creation;
    }

    public byte[] getHashedToken() {
        return hashedToken;
    }

    public boolean validate(long token) {
        byte[] hashedToken = messageDigest.digest(Unpooled.copyLong(token).array());
        boolean validate = Instant.now().isBefore(creation.toInstant().plus(MAX_USER_SESSION_AGE)) && Arrays.equals(this.hashedToken, hashedToken);
        //this session will have already been deleted in db and should be garbage collected right after this, but just in case zero the hashes so it won't work again
        Arrays.fill(this.hashedToken, (byte) 0);
        return validate;
    }

    @Override
    public String toString() {
        return "UserSession{id=" + id + ",user" + user + ",creationTime=" + creation + "}";
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof PersistentUserSession && id == ((PersistentUserSession) object).id;
    }
}
