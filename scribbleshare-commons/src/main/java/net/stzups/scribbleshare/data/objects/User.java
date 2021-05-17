package net.stzups.scribbleshare.data.objects;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class User {
    private final long id;
    private final Set<Long> ownedDocuments;
    private final Set<Long> sharedDocuments;
    private final String username;

    public User(long id, Long[] ownedDocuments, Long[] sharedDocuments, String username) {
        this.id = id;
        this.ownedDocuments = new HashSet<>(Arrays.asList(ownedDocuments));
        this.sharedDocuments = new HashSet<>(Arrays.asList(sharedDocuments));
        this.username = username;
    }

    public long getId() {
        return id;
    }

    public Set<Long> getOwnedDocuments() {
        return ownedDocuments;
    }

    public Set<Long> getSharedDocuments() {
        return sharedDocuments;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ",ownedDocuments=" + ownedDocuments + ",sharedDocuments=" + sharedDocuments + "}";
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }


    @Override
    public boolean equals(Object object) {
        return object instanceof User && id == ((User) object).id;
    }
}
