package com.scaler.userservicemwfeve.services;

import com.scaler.userservicemwfeve.exceptions.TokenExpiredException;
import com.scaler.userservicemwfeve.exceptions.TokenNotFoundException;
import com.scaler.userservicemwfeve.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private static final int EXPIRY_DAYS = 30;

    private final SecretKey key;

    public JwtService(@Value("${jwt.secret:userservicemwfeve-secret-key-at-least-256-bits-for-hs256}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRY_DAYS * 24L * 60 * 60 * 1000);
        List<String> roleNames = user.getRoles() == null ? List.of() : user.getRoles().stream()
                .map(r -> r.getName())
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("roles", roleNames)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public Claims parseAndValidate(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("Token has expired");
        } catch (JwtException e) {
            throw new TokenNotFoundException("Invalid or expired token");
        }
    }
}
