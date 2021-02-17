package net.stzups.board.data.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Document implements Serializable {
    private long id;
    private User owner;
    private String name;
    private String inviteCode;
    private Map<User, List<Point>> points = new HashMap<>();

    public Document(long id, User owner, String name) {
        this.id = id;
        this.owner = owner;
        owner.getOwnedDocuments().add(id);
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
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
}
