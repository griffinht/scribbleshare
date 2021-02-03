package net.stzups.board.data.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class User implements Serializable {
    private static Random random = new Random();

    private HttpSession httpSession;
    private int id;
    private List<String> ownedDocuments;
    private List<String> sharedDocuments;

    public User(HttpSession httpSession) {
        this.httpSession = httpSession;
        id = random.nextInt();
        ownedDocuments = new ArrayList<>();
        //ownedDocuments.add(Document.createDocument(this).getId()); todo do somewhere else
        sharedDocuments = new ArrayList<>();
    }

    public HttpSession getHttpSession() {
        return httpSession;
    }

    public int getId() {
        return id;
    }

    public List<String> getOwnedDocuments() {
        return ownedDocuments;
    }

    public List<String> getSharedDocuments() {
        return sharedDocuments;
    }

    @Override
    public String toString() {
        return "User{id=" + id + "}";
    }
}
