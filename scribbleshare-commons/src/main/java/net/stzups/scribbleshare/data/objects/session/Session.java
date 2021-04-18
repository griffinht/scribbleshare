package net.stzups.scribbleshare.data.objects.session;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;

public class Session {
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
    private final long user;
    private final Timestamp creation;
    private byte[] hashedToken;

    protected Session(long user) {
        this.id = secureRandom.nextLong();
        this.user = user;
        this.creation = new Timestamp(Instant.now().toEpochMilli());
    }

    protected Session(long id, long user, Timestamp creation, byte[] hashedToken) {
        this.id = id;
        this.user = user;
        this.creation = creation;
        this.hashedToken = hashedToken;
    }

    public Session(long id, ByteBuf byteBuf) {
        this.id = id;
        this.user = byteBuf.readLong();
        this.creation = Timestamp.from(Instant.ofEpochMilli(byteBuf.readLong()));
        this.hashedToken = Unpooled.copiedBuffer(byteBuf.readBytes(32)).array(); //byteBuf#readBytes() returns direct buffer which does not support #array() method
    }

    /** should be called once after instance creation */
    long generateToken() {
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
        return Arrays.equals(hashedToken, this.hashedToken);
    }

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeLong(user);
        byteBuf.writeLong(creation.getTime());
        byteBuf.writeBytes(hashedToken);
    }

    @Override
    public String toString() {
        return "Session{id=" + id + ",user" + user + ",creationTime=" + creation + "}";
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof Session && id == ((Session) object).id;
    }
}
