package com.scaler.userservicemwfeve.services;

import com.scaler.userservicemwfeve.exceptions.TokenExpiredException;
import com.scaler.userservicemwfeve.models.Role;
import com.scaler.userservicemwfeve.models.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("userservicemwfeve-secret-key-at-least-256-bits-for-hs256");
    }

    @Test
    void createToken_andParse_returnsValidClaims() {
        Role role = new Role();
        role.setName("USER");
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setRoles(List.of(role));

        String token = jwtService.createToken(user);
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);

        Claims claims = jwtService.parseAndValidate(token);
        assertEquals("1", claims.getSubject());
        assertEquals("test@example.com", claims.get("email"));
        assertEquals("Test User", claims.get("name"));
        assertNotNull(claims.getExpiration());
    }
}
