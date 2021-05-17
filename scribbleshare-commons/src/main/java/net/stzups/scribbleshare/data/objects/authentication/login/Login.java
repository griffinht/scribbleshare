package net.stzups.scribbleshare.data.objects.authentication.login;

import at.favre.lib.crypto.bcrypt.BCrypt;

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

    private final long id;
    private final byte[] hashedPassword;

    public Login(long id, byte[] hashedPassword) {
        this.id = id;
        this.hashedPassword = hashedPassword;
    }

    public long getId() {
        return id;
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
