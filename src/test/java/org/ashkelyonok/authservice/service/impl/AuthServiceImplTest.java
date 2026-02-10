package org.ashkelyonok.authservice.service.impl;

import org.ashkelyonok.authservice.client.UserServiceClient;
import org.ashkelyonok.authservice.exception.InvalidTokenException;
import org.ashkelyonok.authservice.exception.TokenRefreshException;
import org.ashkelyonok.authservice.exception.UserAlreadyExistsException;
import org.ashkelyonok.authservice.exception.UserNotFoundException;
import org.ashkelyonok.authservice.mapper.UserCredentialMapper;
import org.ashkelyonok.authservice.model.dto.request.AuthRequestDto;
import org.ashkelyonok.authservice.model.dto.request.RefreshTokenRequestDto;
import org.ashkelyonok.authservice.model.dto.request.RegisterRequestDto;
import org.ashkelyonok.authservice.model.dto.request.TokenValidationRequestDto;
import org.ashkelyonok.authservice.model.dto.response.AuthResponseDto;
import org.ashkelyonok.authservice.model.dto.response.TokenValidationResponseDto;
import org.ashkelyonok.authservice.model.dto.response.UserResponseDto;
import org.ashkelyonok.authservice.model.entity.RefreshToken;
import org.ashkelyonok.authservice.model.entity.UserCredential;
import org.ashkelyonok.authservice.repository.RefreshTokenRepository;
import org.ashkelyonok.authservice.repository.UserCredentialRepository;
import org.ashkelyonok.authservice.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserCredentialRepository credentialRepository;
    @Mock private RefreshTokenRepository tokenRepository;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserCredentialMapper credentialMapper;
    @Mock private UserServiceClient userServiceClient;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("Register: Should Throw Exception if Email Exists")
    void register_EmailExists_ThrowException() {
        RegisterRequestDto req = new RegisterRequestDto();
        req.setUsername("exists");
        req.setEmail("test@test.com");

        when(credentialRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(req));
    }

    @Test
    @DisplayName("Register: Should Success")
    void register_Success() {
        RegisterRequestDto req = new RegisterRequestDto();
        req.setEmail("new@test.com");
        req.setUsername("new_user");
        req.setPassword("pass");

        UserResponseDto mockResponse = new UserResponseDto();
        mockResponse.setId(999L);
        UserCredential credential = new UserCredential();
        credential.setId(1L);
        credential.setUsername("new_user");
        credential.setEmail("new@test.com");

        when(credentialRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userServiceClient.createUser(any())).thenReturn(mockResponse);
        when(credentialMapper.toEntity(any())).thenReturn(credential);
        when(jwtUtil.generateAccessToken(any())).thenReturn("access");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh");

        AuthResponseDto result = authService.register(req);

        assertNotNull(result);
        assertEquals("access", result.getAccessToken());
        verify(userServiceClient).createUser(req);
        verify(credentialRepository).save(any(UserCredential.class));
        verify(tokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Login: Should Success")
    void login_Success() {
        AuthRequestDto req = new AuthRequestDto();
        req.setUsername("user");
        req.setPassword("pass");

        UserCredential credential = new UserCredential();
        credential.setUsername("user");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(credentialRepository.findByUsername(req.getUsername())).thenReturn(Optional.of(credential));
        when(jwtUtil.generateAccessToken(any())).thenReturn("access");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh");

        AuthResponseDto result = authService.login(req);

        assertNotNull(result);
        assertEquals("access", result.getAccessToken());
        assertEquals("refresh", result.getRefreshToken());
        verify(tokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Login: Should Throw UserNotFoundException (Covers lambda$getCredentialByEmail$1)")
    void login_UserNotFound_ThrowException() {
        AuthRequestDto req = new AuthRequestDto();
        req.setUsername("missing");
        req.setPassword("pass");

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(credentialRepository.findByUsername(req.getUsername())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.login(req));
    }

    @Test
    @DisplayName("RefreshToken: Success")
    void refreshToken_Success() {
        RefreshTokenRequestDto req = new RefreshTokenRequestDto();
        req.setRefreshToken("valid_refresh_token");

        RefreshToken storedToken = new RefreshToken();
        storedToken.setRevoked(false);
        storedToken.setCredential(new UserCredential());
        storedToken.setExpirationDate(LocalDateTime.now().plusDays(1));

        when(jwtUtil.isTokenValid("valid_refresh_token")).thenReturn(true);
        when(tokenRepository.findByToken("valid_refresh_token")).thenReturn(Optional.of(storedToken));
        when(jwtUtil.generateAccessToken(any())).thenReturn("new_access");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("new_refresh");

        AuthResponseDto result = authService.refreshToken(req);

        assertNotNull(result);
        assertTrue(storedToken.isRevoked());
        verify(tokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("RefreshToken: Invalid Format Throws Exception")
    void refreshToken_InvalidFormat_ThrowException() {
        RefreshTokenRequestDto req = new RefreshTokenRequestDto();
        req.setRefreshToken("invalid_token");

        when(jwtUtil.isTokenValid("invalid_token")).thenReturn(false);

        assertThrows(InvalidTokenException.class, () -> authService.refreshToken(req));
    }

    @Test
    @DisplayName("RefreshToken: Token Not Found (Covers lambda$refreshToken$0)")
    void refreshToken_TokenNotFound_ThrowException() {
        RefreshTokenRequestDto req = new RefreshTokenRequestDto();
        req.setRefreshToken("unknown_token");

        when(jwtUtil.isTokenValid("unknown_token")).thenReturn(true);
        when(tokenRepository.findByToken("unknown_token")).thenReturn(Optional.empty());

        assertThrows(TokenRefreshException.class, () -> authService.refreshToken(req));
    }

    @Test
    @DisplayName("RefreshToken: Token Revoked Throws Exception")
    void refreshToken_Revoked_ThrowException() {
        RefreshTokenRequestDto req = new RefreshTokenRequestDto();
        req.setRefreshToken("revoked_token");

        RefreshToken storedToken = new RefreshToken();
        storedToken.setRevoked(true);

        when(jwtUtil.isTokenValid("revoked_token")).thenReturn(true);
        when(tokenRepository.findByToken("revoked_token")).thenReturn(Optional.of(storedToken));

        assertThrows(TokenRefreshException.class, () -> authService.refreshToken(req));
    }


    @Test
    @DisplayName("ValidateToken: Valid Token")
    void validateToken_Valid() {
        TokenValidationRequestDto req = new TokenValidationRequestDto();
        req.setToken("valid_token");

        UserCredential user = new UserCredential();
        user.setEnabled(true);
        user.setAccountNonLocked(true);

        when(jwtUtil.isTokenValid("valid_token")).thenReturn(true);
        when(jwtUtil.extractUsername("valid_token")).thenReturn("test@test.com");
        when(credentialRepository.findByUsername("test@test.com")).thenReturn(Optional.of(user));
        when(jwtUtil.extractUserId("valid_token")).thenReturn(123L);
        when(jwtUtil.extractRole("valid_token")).thenReturn("USER");

        TokenValidationResponseDto result = authService.validateToken(req);

        assertTrue(result.isValid());
        assertEquals(123L, result.getUserId());
        assertEquals("USER", result.getRole());
    }

    @Test
    @DisplayName("ValidateToken: Invalid Token")
    void validateToken_Invalid() {
        TokenValidationRequestDto req = new TokenValidationRequestDto();
        req.setToken("invalid_token");

        when(jwtUtil.isTokenValid("invalid_token")).thenReturn(false);

        TokenValidationResponseDto result = authService.validateToken(req);

        assertFalse(result.isValid());
        assertEquals("Invalid Token Signature or Expired", result.getErrorMessage());
    }

    @Test
    @DisplayName("ValidateToken: Exception Handling (Covers catch block)")
    void validateToken_Exception() {
        TokenValidationRequestDto req = new TokenValidationRequestDto();
        req.setToken("error_token");

        when(jwtUtil.isTokenValid("error_token")).thenThrow(new RuntimeException("Unexpected error"));

        TokenValidationResponseDto result = authService.validateToken(req);

        assertFalse(result.isValid());
        assertEquals("Unexpected error", result.getErrorMessage());
    }
}