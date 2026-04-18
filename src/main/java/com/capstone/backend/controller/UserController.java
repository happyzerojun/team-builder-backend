package com.capstone.backend.controller;

import com.capstone.backend.dto.UserProfileUpdateRequestDto;
import com.capstone.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 내 프로필 정보 수정 (PUT 메서드)
    @PutMapping("/me/profile")
    public ResponseEntity<String> updateProfile(
            Authentication authentication, // 시큐리티가 현재 로그인한 유저 정보를 여기에 넣어줍니다.
            @RequestBody UserProfileUpdateRequestDto requestDto) {

        // 현재 로그인한 유저의 이메일 꺼내기
        String email = authentication.getName();

        // 서비스로 넘겨서 업데이트 실행
        userService.updateUserProfile(email, requestDto);

        return ResponseEntity.ok("프로필 업데이트가 완료되었습니다!");
    }
}
