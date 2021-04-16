package net.stzups.board.data.objects;

import java.util.Random;

public class Document {
    private static final Random random = new Random();
    private static final String DEFAULT_DOCUMENT_NAME = "Untitled Document";

    private final long id;
    private final User owner;
    private String name;

    /**
     * New document
     */
    public Document(User owner) {
        this.id = random.nextLong();
        this.owner = owner;
        this.name = DEFAULT_DOCUMENT_NAME;
    }

    /**
     * Serialize document from db
     */
    public Document(long id, User owner, String name) {
        this.id = id;
        this.owner = owner;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        return "Document{id=" + id + ",name=" + name + "}";
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof Document && id == ((Document) object).id;
    }
}
