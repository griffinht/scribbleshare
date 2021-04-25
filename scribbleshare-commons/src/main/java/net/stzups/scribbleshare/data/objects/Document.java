package net.stzups.scribbleshare.data.objects;

import java.util.Random;

public class Document {
    private static final Random random = new Random();
    private static final String DEFAULT_DOCUMENT_NAME = "Untitled Scribble";

    private final long id;
    private final long owner;
    private String name;

    /**
     * New document
     */
    public Document(User owner) {
        id = random.nextLong();
        this.owner = owner.getId();
        name = DEFAULT_DOCUMENT_NAME;
    }

    /**
     * Serialize document from db
     */
    public Document(long id, long owner, String name) {
        this.id = id;
        this.owner = owner;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public long getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
