package net.stzups.board.room;

import java.util.Random;

public class User {
    private static Random random = new Random();

    private int id;

    public User() {
        id = random.nextInt();
    }

    public User(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "User{id=" + id + "}";
    }
}
