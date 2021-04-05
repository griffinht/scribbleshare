package net.stzups.board.data.objects;

public class User {
    private long id;
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

    public void addOwnedDocument(Document document) {
        ownedDocuments = addElement(ownedDocuments, document);
    }

    public Long[] getSharedDocuments() {
        return sharedDocuments;
    }

    public void addSharedDocument(Document document) {
        sharedDocuments = addElement(sharedDocuments, document);
    }

    private Long[] addElement(Long[] oldLongs, Document element) {
        Long[] newLongs = new Long[oldLongs.length + 1];
        System.arraycopy(oldLongs, 0, newLongs, 0, oldLongs.length);
        newLongs[newLongs.length - 1] = element.getId();
        return newLongs;
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
