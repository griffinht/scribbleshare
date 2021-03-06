package net.stzups.board.data.objects;

import net.stzups.board.data.TokenGenerator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private long id;
    private List<Long> ownedDocuments;
    private List<Long> sharedDocuments;

    public User() {
        id = TokenGenerator.getSecureRandom().nextLong();
        ownedDocuments = new ArrayList<>();
        //ownedDocuments.add(Document.createDocument(this).getId()); todo do somewhere else
        sharedDocuments = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public List<Long> getOwnedDocuments() {
        return ownedDocuments;
    }

    public List<Long> getSharedDocuments() {
        return sharedDocuments;
    }

    @Override
    public String toString() {
        return "User{id=" + id + "}";
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
