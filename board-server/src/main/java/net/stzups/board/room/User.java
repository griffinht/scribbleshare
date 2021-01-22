package net.stzups.board.room;

import java.util.Random;

public class User {
    private static Random random = new Random();

    private long id;

    public User() {
        id = random.nextLong();
    }

    public User(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "User{id=" + id + "}";
    }
}
