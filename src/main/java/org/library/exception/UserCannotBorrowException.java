package org.library.exception;

public class UserCannotBorrowException extends Exception {
    public UserCannotBorrowException(String message) {
        super(message);
    }
}
