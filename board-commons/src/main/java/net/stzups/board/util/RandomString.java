package net.stzups.board.util;

import java.util.Random;

public class RandomString {
    public static final char[] LOWERCASE_ALPHABET = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    public static final char[] NUMERIC = "0123456789".toCharArray();

    private static final Random random = new Random();

    public static String randomString(int length, char[] chars) {
        char[] randomChars = new char[length];
        for (int i = 0; i < randomChars.length; i++) {
            randomChars[i] = chars[random.nextInt(chars.length)];
        }
        return new String(randomChars);
    }
}
