package net.stzups.board.data.objects;

import io.netty.buffer.ByteBuf;
import net.stzups.board.BoardRoom;
import net.stzups.board.data.objects.canvas.Canvas;

public class Document {
    private static final String DEFAULT_DOCUMENT_NAME = "Untitled Document";

    private long id;
    private User owner;
    private String name;
    private String inviteCode;

    /**
     * New document
     */
    public Document(User owner) {
        this.id = BoardRoom.getRandom().nextLong();
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
}
