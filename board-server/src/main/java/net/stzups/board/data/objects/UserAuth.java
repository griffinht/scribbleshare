package net.stzups.board.data.objects;

public class UserAuth {
    private int id;
    private byte[] hash;

    public UserAuth(int id, byte[] hash) {
        this.id = id;
        this.hash = hash;
    }
}
