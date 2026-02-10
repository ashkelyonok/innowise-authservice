package org.ashkelyonok.authservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.ashkelyonok.authservice.service.AuthService;
import org.ashkelyonok.authservice.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserCredentialRepository credentialRepository;
    private final RefreshTokenRepository tokenRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserCredentialMapper credentialMapper;
    private final UserServiceClient userServiceClient;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Override
    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {
        log.info("Registering user: {}", request.getEmail());

        if (credentialRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already in use: " + request.getEmail());
        }

        UserResponseDto userResponse = userServiceClient.createUser(request);
        Long userId = userResponse.getId();

        UserCredential credential = credentialMapper.toEntity(request);
        credential.setUserId(userId);

        credentialRepository.save(credential);
        log.info("User registered with internal ID: {}", credential.getId());

        return generateTokens(credential);
    }

    @Override
    @Transactional
    public AuthResponseDto login(AuthRequestDto request) {
        log.info("Authenticating user: {}", request.getUsername());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserCredential user = getCredentialByUsername(request.getUsername());

        return generateTokens(user);
    }

    @Override
    @Transactional
    public AuthResponseDto refreshToken(RefreshTokenRequestDto request) {
        String token = request.getRefreshToken();
        log.debug("Processing refresh token request");

        if (!jwtUtil.isTokenValid(token)) {
            throw new InvalidTokenException("Invalid refresh token format");
        }

        RefreshToken storedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenRefreshException(token, "Token not found"));

        if (storedToken.isRevoked()) {
            throw new TokenRefreshException(token, "Token was revoked");
        }

        if (storedToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(storedToken);
            throw new TokenRefreshException(token, "Refresh token expired");
        }

        storedToken.setRevoked(true);
        tokenRepository.save(storedToken);
        log.debug("Refresh token revoked, generating new tokens");

        return generateTokens(storedToken.getCredential());
    }

    @Override
    public TokenValidationResponseDto validateToken(TokenValidationRequestDto request) {
        log.debug("Validating token");

        try {
            String token = request.getToken();

            if (!jwtUtil.isTokenValid(token)) {
                return buildValidationResponse(false, null, null, "Invalid Token Signature or Expired");
            }

            var storedToken = tokenRepository.findByToken(token).orElse(null);

            if (storedToken != null && storedToken.isRevoked()) {
                return buildValidationResponse(false, null, null, "Token has been revoked");
            }

            String username = jwtUtil.extractUsername(token);
            UserCredential user = credentialRepository.findByUsername(username)
                    .orElse(null);

            if (user == null) {
                return buildValidationResponse(false, null, null, "User no longer exists");
            }

            if (!user.isEnabled() || !user.isAccountNonLocked()) {
                return buildValidationResponse(false, null, null, "Account is locked or disabled");
            }

            return buildValidationResponse(
                    true,
                    jwtUtil.extractUserId(token),
                    jwtUtil.extractRole(token),
                    null
            );

        } catch (Exception e) {
            log.warn("Token validation error: {}", e.getMessage());
            return buildValidationResponse(false, null, null, e.getMessage());
        }
    }

    private UserCredential getCredentialByUsername(String username) {
        return credentialRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }

    private AuthResponseDto generateTokens(UserCredential user) {
        String access = jwtUtil.generateAccessToken(user);
        String refresh = jwtUtil.generateRefreshToken(user);

        saveRefreshToken(user, refresh);

        return AuthResponseDto.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .build();
    }

    private void saveRefreshToken(UserCredential user, String token) {
        LocalDateTime expiryDate = LocalDateTime.now().plus(java.time.Duration.ofMillis(refreshExpiration));

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .credential(user)
                .expirationDate(expiryDate)
                .revoked(false)
                .build();
        tokenRepository.save(refreshToken);
    }

    private TokenValidationResponseDto buildValidationResponse(boolean valid, Long userId, String role, String error) {
        return TokenValidationResponseDto.builder()
                .valid(valid)
                .userId(userId)
                .role(role)
                .errorMessage(error)
                .build();
    }
}