package com.capstone.backend.service;

import com.capstone.backend.dto.AuthMeResponse;
import com.capstone.backend.dto.LoginRequest;
import com.capstone.backend.dto.SignupRequest;
import com.capstone.backend.entity.AuthProvider;
import com.capstone.backend.entity.User;
import com.capstone.backend.global.exception.ConflictException;
import com.capstone.backend.global.exception.UnauthorizedException;
import com.capstone.backend.global.jwt.JwtUtil;
import com.capstone.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public User signup(SignupRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("이미 존재하는 이메일입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .build();

        return userRepository.save(user);
    }

    // 로그인
    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (user.getProvider() != AuthProvider.LOCAL || user.getPassword() == null) {
            throw new UnauthorizedException("OAuth 계정입니다. 소셜 로그인을 사용해주세요.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return jwtUtil.createToken(user.getEmail()); // ⭐ 토큰 반환
    }

    public AuthMeResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("사용자 정보를 찾을 수 없습니다."));

        return new AuthMeResponse(user.getId(), user.getEmail(), user.getName());
    }
}
