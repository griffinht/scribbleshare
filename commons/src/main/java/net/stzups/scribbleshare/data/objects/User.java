package net.stzups.scribbleshare.data.objects;

import net.stzups.util.DebugString;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class User {
    private static final Random RANDOM = new Random();
    private final long id;
    private final Set<Long> ownedDocuments;
    private final Set<Long> sharedDocuments;
    private final String username;

    public User() {
        this("");
    }

    public User(String username) {
        this(RANDOM.nextLong(), new Long[0], new Long[0], username);
    }
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

    public boolean isRegistered() {
        return !username.equals("");
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return DebugString.get(User.class)
                .add("id", id)
                .add("ownedDocuments", ownedDocuments)
                .add("sharedDocuments", sharedDocuments)
                .toString();
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
