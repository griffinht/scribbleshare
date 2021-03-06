package net.stzups.board.data;

import java.security.SecureRandom;
import java.util.Random;

public class TokenGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Random random = new Random();

    public static SecureRandom getSecureRandom() {
        return secureRandom;
    }

    public static Random getRandom() {
        return random;
    }
}
