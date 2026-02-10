package org.ashkelyonok.authservice.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ashkelyonok.authservice.model.dto.error.ErrorResponseDto;
import org.ashkelyonok.authservice.model.dto.error.ValidationErrorResponseDto;
import org.ashkelyonok.authservice.model.dto.request.AuthRequestDto;
import org.ashkelyonok.authservice.model.dto.request.RefreshTokenRequestDto;
import org.ashkelyonok.authservice.model.dto.request.RegisterRequestDto;
import org.ashkelyonok.authservice.model.dto.request.TokenValidationRequestDto;
import org.ashkelyonok.authservice.model.dto.response.AuthResponseDto;
import org.ashkelyonok.authservice.model.dto.response.TokenValidationResponseDto;
import org.springframework.http.ResponseEntity;

@Tag(name = "Authentication", description = "Endpoints for Login, Registration, and Token Management")
public interface AuthControllerApi {

    @Operation(summary = "Register a new user", description = "Creates a new user profile and credentials.")
    @ApiResponse(responseCode = "200", description = "Registration successful", content = @Content(schema = @Schema(implementation = AuthResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content(schema = @Schema(implementation = ValidationErrorResponseDto.class)))
    @ApiResponse(responseCode = "409", description = "User already exists", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    ResponseEntity<AuthResponseDto> register(RegisterRequestDto request);

    @Operation(summary = "Login user", description = "Authenticates user credentials and returns tokens.")
    @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = AuthResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content(schema = @Schema(implementation = ValidationErrorResponseDto.class)))
    @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    ResponseEntity<AuthResponseDto> login(AuthRequestDto request);

    @Operation(summary = "Refresh Access Token", description = "Issues a new Access Token using a valid Refresh Token.")
    @ApiResponse(responseCode = "200", description = "Token refreshed successfully", content = @Content(schema = @Schema(implementation = AuthResponseDto.class)))
    @ApiResponse(responseCode = "401", description = "Invalid Token format", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "403", description = "Token revoked or expired", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    ResponseEntity<AuthResponseDto> refresh(RefreshTokenRequestDto request);

    @Operation(summary = "Validate Token", description = "Checks if the provided token is valid (for internal service use).")
    @ApiResponse(responseCode = "200", description = "Validation check performed", content = @Content(schema = @Schema(implementation = TokenValidationResponseDto.class)))
    ResponseEntity<TokenValidationResponseDto> validate(TokenValidationRequestDto request);
}
