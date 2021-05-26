package net.stzups.scribbleshare.data.database.databases;

import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.User;
import org.jetbrains.annotations.Nullable;

public interface DocumentDatabase {
    Document createDocument(User owner) throws DatabaseException;

    /**
     * @param id of document
     * @return null if {@link Document} does not exist
     */
    @Nullable Document getDocument(long id);

    void updateDocument(Document document) throws DatabaseException;
    //todo fail silently or throw exception if document does not exist?
    void deleteDocument(Document document) throws DatabaseException;
}
