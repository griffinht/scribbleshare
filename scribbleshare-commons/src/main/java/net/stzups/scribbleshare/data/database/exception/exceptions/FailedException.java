package net.stzups.scribbleshare.data.database.exception.exceptions;

import net.stzups.scribbleshare.data.database.exception.DatabaseException;

public class FailedException extends DatabaseException {
    public FailedException(Throwable cause) {
        super(cause);
    }
}
