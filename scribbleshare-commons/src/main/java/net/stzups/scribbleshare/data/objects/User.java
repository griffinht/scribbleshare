package net.stzups.scribbleshare.data.objects;

import java.util.Arrays;

public class User {
    private final long id;
    private Long[] ownedDocuments;
    private Long[] sharedDocuments;

    public User(long id, Long[] ownedDocuments, Long[] sharedDocuments) {
        this.id = id;
        this.ownedDocuments = ownedDocuments;
        this.sharedDocuments = sharedDocuments;
    }

    public long getId() {
        return id;
    }

    public Long[] getOwnedDocuments() {
        return ownedDocuments;
    }

    public boolean addOwnedDocument(Document document) {
        if (contains(ownedDocuments, document.getId())) {
            return false;
        } else {
            ownedDocuments = addElement(ownedDocuments, document);
            return true;
        }
    }

    public Long[] getSharedDocuments() {
        return sharedDocuments;
    }

    public boolean addSharedDocument(Document document) {
        if (contains(sharedDocuments, document.getId())) {
            return false;
        } else {
            sharedDocuments = addElement(sharedDocuments, document);
            return true;
        }
    }

    private boolean contains(Long[] longs, Long check) {
        for (long l : longs) {
            if (l == check) return true;
        }
        return false;
    }
    private Long[] addElement(Long[] oldLongs, Document element) {
        Long[] newLongs = new Long[oldLongs.length + 1];
        System.arraycopy(oldLongs, 0, newLongs, 0, oldLongs.length);
        newLongs[newLongs.length - 1] = element.getId();
        return newLongs;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ",ownedDocuments=" + Arrays.toString(ownedDocuments) + ",sharedDocuments=" + Arrays.toString(sharedDocuments) + "}";
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
