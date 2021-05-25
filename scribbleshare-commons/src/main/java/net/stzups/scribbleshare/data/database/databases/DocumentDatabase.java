package net.stzups.scribbleshare.data.database.databases;

import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.User;

public interface DocumentDatabase {
    Document createDocument(User owner);
    Document getDocument(long id);
    void updateDocument(Document document);
    void deleteDocument(Document document);
}
