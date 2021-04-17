package net.stzups.scribbleshare.data.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Document {
    private static final Random random = new Random();
    private static final String DEFAULT_DOCUMENT_NAME = "Untitled Document";

    private final long id;
    private final long owner;
    private final List<Long> resources;
    private String name;

    /**
     * New document
     */
    public Document(User owner) {
        id = random.nextLong();
        this.owner = owner.getId();
        resources = new ArrayList<>();
        name = DEFAULT_DOCUMENT_NAME;
    }

    /**
     * Serialize document from db
     */
    public Document(long id, long owner, List<Long> resources, String name) {
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.resources = resources;
    }

    public long getId() {
        return id;
    }

    public long getOwner() {
        return owner;
    }

    public List<Long> getResources() {
        return resources;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Document{id=" + id + ",name=" + name + ",resources=" + resources + "}";
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
