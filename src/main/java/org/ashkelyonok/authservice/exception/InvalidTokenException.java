package org.ashkelyonok.authservice.exception;

import java.io.Serial;

public class InvalidTokenException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 8361045496408354187L;

    public InvalidTokenException() {
        super("Authentication failed: Invalid or expired token provided");
    }

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}