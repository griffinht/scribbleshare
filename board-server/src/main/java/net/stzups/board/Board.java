package net.stzups.board;

public class Board {
    public static void main(String[] args) {
        System.out.println("Starting Board...");

        long start = System.currentTimeMillis();

        new HTTPServer();

        System.out.println("Started Board in " + (System.currentTimeMillis() - start) + "ms");
    }
}
