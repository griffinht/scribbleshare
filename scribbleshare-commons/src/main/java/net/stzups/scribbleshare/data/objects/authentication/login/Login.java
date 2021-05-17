package net.stzups.scribbleshare.data.objects.authentication.login;

import at.favre.lib.crypto.bcrypt.BCrypt;
import net.stzups.scribbleshare.data.objects.User;

import java.util.Arrays;

public class Login {
    private static final byte[] DUMMY;
    static {
        DUMMY = BCrypt.withDefaults().hash(6, new byte[0]);
    }
    private static byte[] getDummy() {
        byte[] dummy = new byte[DUMMY.length];
        System.arraycopy(DUMMY, 0, dummy, 0, dummy.length);
        return dummy;
    }

    private final String username;
    private final long id;
    private final byte[] hashedPassword;

    public Login(User user, byte[] password) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.hashedPassword = BCrypt.withDefaults().hash(6, password);
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

    public static Long verify(Login login, byte[] plaintext) {
        byte[] hashedPassword;
        if (login == null) {
            hashedPassword = getDummy();//todo is this helpful in a hypothetical timing attack?
        } else {
            hashedPassword = login.hashedPassword;
        }

        boolean verified = BCrypt.verifyer().verify(plaintext, hashedPassword).verified;

        Arrays.fill(hashedPassword, (byte) 0);
        Arrays.fill(plaintext, (byte) 0);

        if (verified) {
            assert login != null;
            return login.id;
        } else {
            return null;
        }
    }
}
