package com.capstone.backend.service;

import com.capstone.backend.dto.LoginRequest;
import com.capstone.backend.dto.SignupRequest;
import com.capstone.backend.entity.AuthProvider;
import com.capstone.backend.entity.User;
import com.capstone.backend.global.exception.UnauthorizedException;
import com.capstone.backend.global.jwt.JwtUtil;
import com.capstone.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    private final JwtUtil jwtUtil = new JwtUtil("mysecretkeymysecretkeymysecretkey", 3600000);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void loginReturnsJwtForLocalUser() {
        AuthService authService = new AuthService(userRepository, jwtUtil, passwordEncoder);
        LoginRequest request = new LoginRequest();
        ReflectionTestUtils.setField(request, "email", "user@example.com");
        ReflectionTestUtils.setField(request, "password", "pw1234");

        User user = User.builder()
                .email("user@example.com")
                .password(passwordEncoder.encode("pw1234"))
                .name("User")
                .provider(AuthProvider.LOCAL)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        String token = authService.login(request);

        assertEquals("user@example.com", jwtUtil.getEmail(token));
    }

    @Test
    void loginRejectsOAuthAccountInPasswordLogin() {
        AuthService authService = new AuthService(userRepository, jwtUtil, passwordEncoder);
        LoginRequest request = new LoginRequest();
        ReflectionTestUtils.setField(request, "email", "user@example.com");
        ReflectionTestUtils.setField(request, "password", "pw1234");

        User user = User.builder()
                .email("user@example.com")
                .name("User")
                .provider(AuthProvider.GOOGLE)
                .providerId("google-123")
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> authService.login(request));
        assertEquals("OAuth 계정입니다. 소셜 로그인을 사용해주세요.", exception.getMessage());
    }

    @Test
    void signupEncodesPassword() {
        AuthService authService = new AuthService(userRepository, jwtUtil, passwordEncoder);
        SignupRequest request = new SignupRequest();
        ReflectionTestUtils.setField(request, "email", "new@example.com");
        ReflectionTestUtils.setField(request, "password", "pw1234");
        ReflectionTestUtils.setField(request, "name", "New User");

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = authService.signup(request);

        assertEquals(AuthProvider.LOCAL, saved.getProvider());
        assertTrue(passwordEncoder.matches("pw1234", saved.getPassword()));
    }
}
