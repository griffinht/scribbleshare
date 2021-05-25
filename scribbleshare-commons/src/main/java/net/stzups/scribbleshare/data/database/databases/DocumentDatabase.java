package net.stzups.scribbleshare.data.database.databases;

import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.User;

public interface DocumentDatabase {
    Document createDocument(User owner) throws DatabaseException;
    Document getDocument(long id);
    void updateDocument(Document document) throws DatabaseException;
    void deleteDocument(Document document) throws DatabaseException;
}
