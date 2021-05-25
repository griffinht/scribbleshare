package net.stzups.scribbleshare.data.objects.authentication;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpSessionCookie;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;

public class UserSession {
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
    private final Timestamp expired;
    private final long userId;
    private final byte[] hashedToken = new byte[HASHED_TOKEN_LENGTH];

    protected UserSession(long userId) {
        id = secureRandom.nextLong();
        created = new Timestamp(Instant.now().toEpochMilli());
        expired = created;
        this.userId = userId;
    }

    public UserSession(long id, Timestamp created, Timestamp expired, long userId, ByteBuf byteBuf) {
        this.id = id;
        this.created = created;
        this.expired = expired;
        this.userId = userId;
        byteBuf.readBytes(hashedToken);
    }

    protected byte[] generateToken() {
        byte[] token = new byte[HttpSessionCookie.TOKEN_LENGTH];
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

    public Timestamp getExpired() {
        return expired;
    }

    public byte[] getHashedToken() {
        return hashedToken;
    }

    protected void validate(byte[] token) throws AuthenticationException {
        if (!Arrays.equals(messageDigest.digest(token), this.hashedToken))
            throw new AuthenticationException("Bad token");

        if (!created.equals(expired))
            throw new AuthenticationException("Expired session");
    }

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeBytes(hashedToken);
    }

    @Override
    public String toString() {
        return "Session{id=" + id + ",userId" + userId + ",created=" + created + ",expires=" + expired + "}";
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
