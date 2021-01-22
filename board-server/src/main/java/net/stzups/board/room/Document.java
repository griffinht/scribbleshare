package net.stzups.board.room;

import net.stzups.board.protocol.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Document {
    private static final int DOCUMENT_ID_LENGTH = 6;

    private static Map<String, Document> documents = new HashMap<>();

    static Document getDocument(String id) {
        return documents.get(id);
    }
    static Document createDocument(String name) {
        String id;
        int a = 0;
        do {
            id = String.valueOf((int) (Math.random() * Math.pow(10, DOCUMENT_ID_LENGTH)));
            if (a++ > 1000) throw new RuntimeException("infinite loop while making unique document name of " + DOCUMENT_ID_LENGTH + ", current " + id);
        } while (documents.containsKey(id)); //todo improve
        Document document = new Document(id, name);
        documents.put(document.getId(), document);
        return document;
    }


    private String id;
    private String name;
    private Map<User, List<Point>> points = new HashMap<>();

    private Document(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
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
