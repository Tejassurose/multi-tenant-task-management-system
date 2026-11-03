package com.techpulse.taskmanagement.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.techpulse.taskmanagement.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.jwt.expiration}")
    private long jwtExpiration;

    // Generate the signing key from the Base64 secret
    private SecretKey getSigningKey() {

        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Generate JWT token
    public String generateToken(User user) {

        Date currentDate = new Date();

        Date expiryDate = new Date(
                currentDate.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("tenant_id", user.getTenant().getId())
                .claim("role", user.getRole().name())
                .issuedAt(currentDate)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    // Extract all claims after verifying the token
    public Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Extract email
    public String extractEmail(String token) {

        return extractAllClaims(token).getSubject();
    }

    // Extract tenant ID
    public Long extractTenantId(String token) {

        return extractAllClaims(token).get("tenant_id", Long.class);
    }

    // Extract role
    public String extractRole(String token) {

        return extractAllClaims(token).get("role", String.class);
    }

    // Validate token
    public boolean isTokenValid(String token) {

        try {
            Claims claims = extractAllClaims(token);

            return claims.getSubject() != null
                    && claims.getExpiration() != null
                    && claims.getExpiration().after(new Date());

        } catch (Exception exception) {
            return false;
        }
    }
}