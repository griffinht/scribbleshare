package net.stzups.scribbleshare.data.database.exception;

/**
 * Thrown when an exception occurs while establishing a connection to the database
 * Should only be thrown when the database is initialized. If the database connections fails later, it should throw a {@link DatabaseException}
 */
public class ConnectionException extends DatabaseException {
    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
