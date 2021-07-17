package net.stzups.scribbleshare.data.objects.authentication.login;

import at.favre.lib.crypto.bcrypt.BCrypt;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.util.DebugString;

import java.util.Arrays;

public class Login {
    private static final int COST = 6; // todo

    private static final BCrypt.Hasher HASHER = BCrypt.withDefaults();
    private static final BCrypt.Verifyer VERIFIER = BCrypt.verifyer();

    private static final byte[] DUMMY = new byte[0];
    private static final byte[] HASHED_DUMMY;
    static {
        HASHED_DUMMY = HASHER.hash(COST, DUMMY); // todo new byte[0] - use a different dummy plaintext?
    }

    private final String username;
    private final long id;
    private final byte[] hashedPassword;

    public Login(User user, byte[] password) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.hashedPassword = HASHER.hash(COST, password);
        Arrays.fill(password, (byte) 0);
    }

    public Login(String username, long id, byte[] hashedPassword) {
        this.username = username;
        this.id = id;
        this.hashedPassword = hashedPassword;
    }

    public String getUsername() {
        return username;
    }

    public long getId() {
        return id;
    }

    public byte[] getHashedPassword() {
        return hashedPassword;
    }

    public static boolean verify(Login login, byte[] plaintext) {
        byte[] hashedPassword;
        if (login == null) {
            hashedPassword = HASHED_DUMMY; // still verify hash even when we know it will fail to protect against timing attack
        } else {
            hashedPassword = login.hashedPassword;
        }

        boolean verified = VERIFIER.verify(plaintext, hashedPassword).verified;

        if (Arrays.equals(plaintext, DUMMY)) {
            assert !verified : "Dummy should be unverified";
            return false;
        }

        // might as well clear the tokens after using them, as they should only be verified once and discarded
        Arrays.fill(plaintext, (byte) 0);
        if (hashedPassword != HASHED_DUMMY) { // don't clear dummy, it will be reused
            Arrays.fill(hashedPassword, (byte) 0);
        }


        if (login == null) {
            assert !verified : "Null login should be unverified";
            return false;
        }

        return verified;
    }

    @Override
    public String toString() {
        return DebugString.get(Login.class)
                .add("username", username)
                .add("id", id)
                .toString();
    }
}
