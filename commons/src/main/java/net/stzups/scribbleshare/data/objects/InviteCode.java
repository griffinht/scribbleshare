package net.stzups.scribbleshare.data.objects;

import net.stzups.scribbleshare.util.RandomString;
import net.stzups.util.DebugString;

public class InviteCode {
    public static final int INVITE_CODE_LENGTH = 6;
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

    @Override
    public String toString() {
        return DebugString.get(InviteCode.class)
                .add("code", code)
                .add("document", document)
                .toString();
    }
}