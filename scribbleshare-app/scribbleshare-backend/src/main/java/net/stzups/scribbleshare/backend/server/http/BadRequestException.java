package net.stzups.scribbleshare.backend.server.http;

public class BadRequestException extends Exception {
    public BadRequestException(String message) {
        super(message);
    }
}
