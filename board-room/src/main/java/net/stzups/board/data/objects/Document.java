package net.stzups.board.data.objects;

import net.stzups.board.BoardRoom;
import net.stzups.board.data.objects.canvas.objects.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Document {
    private static final String DEFAULT_DOCUMENT_NAME = "Untitled Document";

    private long id;
    private User owner;
    private String name;
    private String inviteCode;
    private Map<User, List<Point>> points = new HashMap<>();

    public Document(User owner) {
        this.id = BoardRoom.getSecureRandom().nextLong();
        this.owner = owner;
        owner.getOwnedDocuments().add(id);
        this.name = DEFAULT_DOCUMENT_NAME;
    }

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

    public Map<User, List<Point>> getPoints() {
        return points;
    }

    public void addPoints(User user, Point[] points) {
        List<Point> pts = this.points.get(user);
        if (pts == null) {
            pts = new ArrayList<>();
        }
        pts.addAll(Arrays.asList(points));
        this.points.put(user, pts);
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
