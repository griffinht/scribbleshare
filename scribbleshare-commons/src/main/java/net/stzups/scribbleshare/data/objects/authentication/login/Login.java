package net.stzups.scribbleshare.data.objects.authentication.login;

import at.favre.lib.crypto.bcrypt.BCrypt;
import net.stzups.scribbleshare.data.objects.User;

import java.util.Arrays;

public class Login {
    private static final int COST = 6; // todo

    private static final BCrypt.Hasher HASHER = BCrypt.withDefaults();
    private static final BCrypt.Verifyer VERIFIER = BCrypt.verifyer();

    private static final byte[] DUMMY;
    static {
        DUMMY = HASHER.hash(COST, new byte[0]); // todo new byte[0] - use a different dummy plaintext?
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
            hashedPassword = DUMMY; // still verify hash even when we know it will fail to protect against timing attack
        } else {
            hashedPassword = login.hashedPassword;
        }

        boolean verified = VERIFIER.verify(plaintext, hashedPassword).verified;

        Arrays.fill(plaintext, (byte) 0);
        if (hashedPassword != DUMMY) { // don't clear dummy, it will be reused
            Arrays.fill(hashedPassword, (byte) 0);
        }

        return verified;
    }
}
