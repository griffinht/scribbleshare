package net.stzups.board;

import net.stzups.board.httpserver.HttpServer;

public class Board {
    private static HttpServer httpServer;

    public static void main(String[] args) {
        System.out.println("Starting Board server...");

        long start = System.currentTimeMillis();

        new ConsoleManager();
        
        httpServer = new HttpServer();
        httpServer.run();

        System.out.println("Started Board server in " + (System.currentTimeMillis() - start) + "ms");
    }

    static void stop() {
        System.out.println("Stopping Board server...");

        long start = System.currentTimeMillis();

        httpServer.stop();

        System.out.println("Stopped Board server in " + (System.currentTimeMillis() - start) + "ms");
    }
}
