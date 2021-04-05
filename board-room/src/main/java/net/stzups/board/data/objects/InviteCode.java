package net.stzups.board.data.objects;

import net.stzups.board.util.RandomString;

public class InviteCode {
    private static final int INVITE_CODE_LENGTH = 6;
    String code;
    long document;

    public InviteCode(Document document) {
        this.code = RandomString.randomString(INVITE_CODE_LENGTH, RandomString.LOWERCASE_ALPHABET);
        this.document = document.getId();
    }

    public InviteCode(String code, long document) {
        this.code = code;
        this.document = document;
    }

    public String getCode() {
        return code;
    }

    public long getDocument() {
        return document;
    }
}
