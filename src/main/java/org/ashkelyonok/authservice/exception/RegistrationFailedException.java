package org.ashkelyonok.authservice.exception;

import java.io.Serial;

public class RegistrationFailedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -7189272215889710233L;

    public RegistrationFailedException() {
        super("User registration failed due to an internal server error");
    }

    public RegistrationFailedException(String message) {
        super(message);
    }

    public RegistrationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}