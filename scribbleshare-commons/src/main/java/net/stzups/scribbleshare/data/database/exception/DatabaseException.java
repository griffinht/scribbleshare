package net.stzups.scribbleshare.data.database.exception;

/**
 * Thrown when there is any exception while handling a database transaction.
 */
public class DatabaseException extends Exception {
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
    public DatabaseException(Throwable cause) {
        super(cause);
    }
}
