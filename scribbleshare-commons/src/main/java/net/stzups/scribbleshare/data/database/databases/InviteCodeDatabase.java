package net.stzups.scribbleshare.data.database.databases;

import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.InviteCode;

public interface InviteCodeDatabase {
    InviteCode getInviteCode(String code);
    InviteCode getInviteCode(Document document);
}
