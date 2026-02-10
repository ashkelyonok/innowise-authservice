package org.ashkelyonok.authservice.exception;

import feign.FeignException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.ashkelyonok.authservice.model.dto.error.ErrorResponseDto;
import org.ashkelyonok.authservice.model.dto.error.ValidationErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import io.jsonwebtoken.security.SignatureException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        log.warn("User not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "User Not Found", ex.getMessage(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNoResourceFoundException(NoResourceFoundException ex, HttpServletRequest request) {
        log.debug("Resource not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Endpoint Not Found", ex.getMessage(), request);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleUserAlreadyExists(UserAlreadyExistsException ex, HttpServletRequest request) {
        log.warn("User conflict: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "User Already Exists", ex.getMessage(), request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Invalid credentials: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Authentication Failed", "Invalid email or password", request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDto> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Authentication Failed", ex.getMessage(), request);
    }

    @ExceptionHandler({ExpiredJwtException.class, SignatureException.class, MalformedJwtException.class, InvalidTokenException.class})
    public ResponseEntity<ErrorResponseDto> handleJwtExceptions(RuntimeException ex, HttpServletRequest request) {
        log.warn("JWT Error: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid Token", "The token is expired or invalid", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "Access Denied", "You do not have permission to access this resource", request);
    }

    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<ErrorResponseDto> handleTokenRefreshException(TokenRefreshException ex, HttpServletRequest request) {
        log.warn("Token refresh failed: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "Token Refresh Failed", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation failed: {}", ex.getMessage());

        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing
                ));

        ValidationErrorResponseDto errorResponse = ValidationErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Input validation failed")
                .path(request.getRequestURI())
                .validationErrors(errors)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponseDto> handleFeignException(FeignException ex, HttpServletRequest request) {
        log.error("External Service Error: Status {} - Body {}", ex.status(), ex.contentUTF8());

        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;

        String errorMessage = "External Service Error";

        if (ex.contentUTF8() != null && !ex.contentUTF8().isEmpty()) {
            errorMessage = "Error from User Service: " + ex.contentUTF8();
        }

        return buildResponse(status, "External Dependency Error", errorMessage, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGlobalException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred", request);
    }

    private ResponseEntity<ErrorResponseDto> buildResponse(HttpStatus status, String errorType, String message, HttpServletRequest request) {
        ErrorResponseDto response = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(errorType)
                .message(message)
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(response, status);
    }
}
