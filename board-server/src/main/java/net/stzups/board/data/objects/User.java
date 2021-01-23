package net.stzups.board.data.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class User implements Serializable {
    private static Random random = new Random();

    private int id;
    private List<String> ownedDocuments;
    private List<String> sharedDocuments;

    public User() {
        id = random.nextInt();
        ownedDocuments = new ArrayList<>();
        ownedDocuments.add(Document.createDocument(this).getId());
        sharedDocuments = new ArrayList<>();
    }

    public User(int id, List<String> ownedDocuments, List<String> sharedDocuments) {
        this.id = id;
        this.ownedDocuments = ownedDocuments;
        this.sharedDocuments = sharedDocuments;
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
