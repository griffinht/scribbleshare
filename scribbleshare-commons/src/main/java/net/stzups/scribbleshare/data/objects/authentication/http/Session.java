package net.stzups.scribbleshare.data.objects.authentication.http;

import io.netty.buffer.ByteBuf;

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
        hashedToken = new byte[32];//todo
        byteBuf.readBytes(hashedToken);
    }

    protected int getTokenLength() {
        return 16;//todo
    }

    /** should be called once after instance creation */
    byte[] generateToken() {
        byte[] token = new byte[getTokenLength()];
        secureRandom.nextBytes(token);
        hashedToken = messageDigest.digest(token);
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

    public boolean validate(byte[] token) {
        return Arrays.equals(messageDigest.digest(token), this.hashedToken);
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
