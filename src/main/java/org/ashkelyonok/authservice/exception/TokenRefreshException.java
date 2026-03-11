package org.ashkelyonok.authservice.exception;

import java.io.Serial;

public class TokenRefreshException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -5355262333217911920L;

    public TokenRefreshException() {
        super("Token refresh operation failed");
    }

    public TokenRefreshException(String message) {
        super(message);
    }

    public TokenRefreshException(String message, Throwable cause) {
        super(message, cause);
    }

    public TokenRefreshException(String token, String message) {
        super(String.format("Token refresh failed for token [%s]: %s", token, message));
    }
}