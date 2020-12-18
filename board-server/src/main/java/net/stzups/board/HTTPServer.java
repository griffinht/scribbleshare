package net.stzups.board;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class HTTPServer implements Runnable {
    private static final int PORT = 80;

    public HTTPServer() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Listening on port " + serverSocket.getLocalPort());
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("New connection from " + socket.getRemoteSocketAddress());

                byte[] response = "why hello there".getBytes(StandardCharsets.UTF_8);
                socket.getOutputStream().write(("HTTP/1.1 200 OK\r\n"
                        + "Server: Tanks\r\n"
                        + "Date: " + new Date() + "\r\n"
                        + "Content-type: "
                        + "text-plain"
                        + "\r\n"
                        + "Content-length: " + response.length + "\r\n"
                        + "\r\n").getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().write(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
