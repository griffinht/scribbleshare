package net.stzups.scribbleshare.data.database.databases;

import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.InviteCode;
import org.jetbrains.annotations.Nullable;

public interface InviteCodeDatabase {
    /**
     * @param code code of {@link InviteCode}
     * @return null if the {@link InviteCode} does not exist
     */
    @Nullable InviteCode getInviteCode(String code) throws DatabaseException;

    /**
     * @param document {@link Document} of {@link InviteCode}
     * @return null if the {@link InviteCode} does not exist
     */
    @Nullable InviteCode getInviteCode(Document document) throws DatabaseException;
}
