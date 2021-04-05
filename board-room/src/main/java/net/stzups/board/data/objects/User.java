package net.stzups.board.data.objects;

import java.util.List;

public class User {
    private long id;
    private List<Long> ownedDocuments;
    private List<Long> sharedDocuments;

    public User(long id, List<Long> ownedDocuments, List<Long> sharedDocuments) {
        this.id = id;
        this.ownedDocuments = ownedDocuments;
        this.sharedDocuments = sharedDocuments;
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
        return "User{id=" + id + ",ownedDocuments=" + ownedDocuments + ",sharedDocuments=" + sharedDocuments + "}";
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
