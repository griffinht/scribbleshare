package net.stzups.board.room;

import net.stzups.board.protocol.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Document {
    private static int NEXT_ID = 0;
    private static Map<String, Document> documents = new HashMap<>();

    static Document getDocument(String id) {
        return documents.get(id);
    }

    static Document createDocument(String name) {
        Document document = new Document(Integer.toString(NEXT_ID++), name);
        documents.put(document.getId(), document);
        return document;
    }

    private String id;
    private String name;
    private List<Point> points = new ArrayList<>();

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

    public List<Point> getPoints() {
        return points;
    }
}
