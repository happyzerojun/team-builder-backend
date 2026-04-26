package com.capstone.backend.controller;

import com.capstone.backend.dto.UserProfileUpdateRequestDto;
import com.capstone.backend.entity.User;
import com.capstone.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*") // 프론트엔드 접속 허락
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // UserController.java 내부에 추가

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(Authentication authentication) { // 반환 타입을 ?로 유연하게
        // 1. 보안 체크
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            String email = authentication.getName();
            User user = userService.getUserByEmail(email);

            // 🚨 만약 무한루프 문제가 계속되면, 여기서 DTO로 변환해서 보내는 게 정석입니다!
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            // 백엔드 로그에 에러 원인을 찍어줍니다.
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류: " + e.getMessage());
        }
    }



    @PutMapping("/me/profile")
    public ResponseEntity<?> updateProfile(Authentication authentication, @RequestBody UserProfileUpdateRequestDto requestDto) {
        // 🚨 이 줄을 무조건 1빠따로 추가해 주세요!
        System.out.println("====== [도착] 프론트에서 프로필 수정 요청이 들어왔습니다! ======");

        try {
            // 기존 로직들...
            String email = authentication.getName();
            User updatedUser = userService.updateUserProfile(email, requestDto);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            // 🚨 만약 여기서 에러가 나면 콘솔에 무조건 찍히게 만듭니다.
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 에러");
        }
    }
}