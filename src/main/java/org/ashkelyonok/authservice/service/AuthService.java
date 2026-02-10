package org.ashkelyonok.authservice.service;

import org.ashkelyonok.authservice.model.dto.request.AuthRequestDto;
import org.ashkelyonok.authservice.model.dto.request.RefreshTokenRequestDto;
import org.ashkelyonok.authservice.model.dto.request.RegisterRequestDto;
import org.ashkelyonok.authservice.model.dto.request.TokenValidationRequestDto;
import org.ashkelyonok.authservice.model.dto.response.AuthResponseDto;
import org.ashkelyonok.authservice.model.dto.response.TokenValidationResponseDto;

/**
 * Service interface for handling Authentication and Authorization logic.
 * Responsible for user registration, login, token refreshing, and token validation.
 */
public interface AuthService {

    /**
     * Registers a new user in the system.
     * <p>
     * This method orchestrates the process by:
     * 1. Checking if the email is already taken.
     * 2. Calling the User Service (via Feign) to create the user profile.
     * 3. Saving the secure credentials locally.
     * 4. generating initial Access and Refresh tokens.
     * </p>
     *
     * @param request DTO containing full registration details (Name, Surname, Email, Password, Role).
     * @return AuthResponseDto containing the generated JWT Access and Refresh tokens.
     */
    AuthResponseDto register(RegisterRequestDto request);

    /**
     * Authenticates a user based on email and password.
     *
     * @param request DTO containing login credentials (email and password).
     * @return AuthResponseDto containing new JWT Access and Refresh tokens.
     */
    AuthResponseDto login(AuthRequestDto request);

    /**
     * Refreshes an expired Access Token using a valid Refresh Token.
     * <p>
     * Checks if the refresh token is valid, exists in the database, and is not revoked.
     * Implements "Token Rotation" by revoking the used refresh token and issuing a new pair.
     * </p>
     *
     * @param request DTO containing the existing Refresh Token.
     * @return AuthResponseDto containing a NEW pair of Access and Refresh tokens.
     */
    AuthResponseDto refreshToken(RefreshTokenRequestDto request);

    /**
     * Validates a JWT token for internal service usage.
     * <p>
     * Used by other microservices (like User Service or Gateway) to verify if a token
     * is legitimate, not expired, and contains the correct user claims.
     * </p>
     *
     * @param request DTO containing the JWT token string.
     * @return TokenValidationResponseDto containing validity status, user ID, and role.
     */
    TokenValidationResponseDto validateToken(TokenValidationRequestDto request);
}
