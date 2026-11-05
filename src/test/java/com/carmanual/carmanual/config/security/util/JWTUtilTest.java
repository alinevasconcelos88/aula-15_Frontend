package com.carmanual.carmanual.config.security.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JWTUtilTest {

    private static final String SECRET = "01234567890123456789012345678901";

    private final JWTUtil jwtUtil = new JWTUtil(SECRET);

    @Test
    void generatesAndReadsUsernameFromValidToken() {
        String token = jwtUtil.generateToken("usuario@example.com");

        assertTrue(jwtUtil.validateToken(token));
        assertEquals("usuario@example.com", jwtUtil.getUsernameFromToken(token));
    }

    @Test
    void rejectsTokenSignedWithDifferentSecret() {
        String token = jwtUtil.generateToken("usuario@example.com");
        JWTUtil otherJwtUtil = new JWTUtil("abcdefghijklmnopqrstuvwxyz123456");

        assertFalse(otherJwtUtil.validateToken(token));
    }
}