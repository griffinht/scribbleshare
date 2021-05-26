package net.stzups.scribbleshare.data.database.databases;

import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.InviteCode;
import org.jetbrains.annotations.Nullable;

public interface InviteCodeDatabase {
    /**
     * @param code {@link InviteCode}
     * @return null if the {@link InviteCode} does not exist for
     */
    @Nullable InviteCode getInviteCode(String code);

    /**
     * @param document {@link Document}
     * @return null if the {@link InviteCode} does not exist
     */
    @Nullable InviteCode getInviteCode(Document document);
}
