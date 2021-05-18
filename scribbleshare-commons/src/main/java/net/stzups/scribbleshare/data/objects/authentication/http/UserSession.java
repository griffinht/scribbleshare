package net.stzups.scribbleshare.data.objects.authentication.http;

import io.netty.buffer.ByteBuf;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;

public class UserSession {
    protected static final int TOKEN_LENGTH = 16;
    private static final int HASHED_TOKEN_LENGTH = 32;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final MessageDigest messageDigest;

    static {
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private final long id;
    private final Timestamp created;
    private final Timestamp expires;
    private final long userId;
    private final byte[] hashedToken = new byte[HASHED_TOKEN_LENGTH];

    protected UserSession(long userId, TemporalAmount age) {
        id = secureRandom.nextLong();
        created = new Timestamp(Instant.now().toEpochMilli());
        expires = new Timestamp(created.toInstant().plus(age).toEpochMilli());
        this.userId = userId;
    }

    public UserSession(long id, Timestamp created, Timestamp expires, long userId, ByteBuf byteBuf) {
        this.id = id;
        this.created = created;
        this.expires = expires;
        this.userId = userId;
        byteBuf.readBytes(hashedToken);
    }

    protected byte[] generateToken() {
        byte[] token = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(token);

        System.arraycopy(messageDigest.digest(token), 0, hashedToken, 0, hashedToken.length);
        return token;
    }

    public long getId() {
        return id;
    }

    public long getUser() {
        return userId;
    }

    public Timestamp getCreated() {
        return created;
    }

    public byte[] getHashedToken() {
        return hashedToken;
    }

    public boolean validate(byte[] token) {
        return Arrays.equals(messageDigest.digest(token), this.hashedToken)
                && Instant.now().isBefore(expires.toInstant());
    }

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeBytes(hashedToken);
    }

    @Override
    public String toString() {
        return "Session{id=" + id + ",userId" + userId + ",created=" + created + ",expires=" + expires + "}";
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof UserSession && id == ((UserSession) object).id;
    }
}
