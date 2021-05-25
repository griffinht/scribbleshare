package net.stzups.scribbleshare.data.database.exception;

public class DatabaseException extends Exception {
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
    public DatabaseException(Throwable cause) {
        super(cause);
    }
}
