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
    private Canvas canvas;

    public Document(User owner) {
        this.id = BoardRoom.getSecureRandom().nextLong();
        this.owner = owner;
        owner.getOwnedDocuments().add(id);
        this.name = DEFAULT_DOCUMENT_NAME;
    }

    public Document(long id, User owner, String name, ByteBuf byteBuf) {
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.canvas = new Canvas(byteBuf);
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

    public Canvas getCanvas() {
        return canvas;
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
