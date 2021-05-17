package net.stzups.scribbleshare.data.objects.authentication.login;

import at.favre.lib.crypto.bcrypt.BCrypt;

import java.util.Arrays;

public class Login {
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
            hashedPassword = new byte[0];
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
