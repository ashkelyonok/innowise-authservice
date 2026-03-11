package org.ashkelyonok.authservice.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.ashkelyonok.authservice.model.entity.UserCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for handling JWT operations.
 * Responsible for generating, signing, parsing, and validating tokens.
 * 1. Generates Access and Refresh tokens.
 * 2. Embeds 'userId' and 'role' into the token claims.
 * 3. Signs tokens using HMAC-SHA256 (HS256).
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateAccessToken(UserCredential user) {
        return buildToken(new HashMap<>(), user, jwtExpiration);
    }

    public String generateRefreshToken(UserCredential user) {
        return buildToken(new HashMap<>(), user, refreshExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, UserCredential user, long expiration) {
        extraClaims.put("userId", user.getUserId());
        extraClaims.put("role", "ROLE_" + user.getRole().name());

        return Jwts.builder()
                .claims(extraClaims)
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    private JsonNode getPayload(String token) {
        try {
            String[] chunks = token.split("\\.");
            String payload = new String(java.util.Base64.getUrlDecoder().decode(chunks[1]));
            return objectMapper.readTree(payload);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token format");
        }
    }

    public String extractUsername(String token) {
        return getPayload(token).get("sub").asText();
    }

    public Long extractUserId(String token) {
        return getPayload(token).get("userId").asLong();
    }

    public String extractRole(String token) {
        return getPayload(token).get("role").asText();
    }

    public boolean isTokenValid(String token) {
        try {
            long expSeconds = getPayload(token).get("exp").asLong();
            Date expirationDate = new Date(expSeconds * 1000);
            return expirationDate.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
