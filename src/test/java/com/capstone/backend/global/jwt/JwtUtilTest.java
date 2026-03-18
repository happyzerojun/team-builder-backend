package com.capstone.backend.global.jwt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil("mysecretkeymysecretkeymysecretkey", 3600000);

    @Test
    void createTokenProducesValidTokenWithEmailSubject() {
        String token = jwtUtil.createToken("user@example.com");

        assertTrue(jwtUtil.validateToken(token));
        assertEquals("user@example.com", jwtUtil.getEmail(token));
    }
}
