package com.capstone.backend.controller;

import com.capstone.backend.dto.UserProfileResponseDto;
import com.capstone.backend.dto.UserProfileUpdateRequest;
import com.capstone.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping({"/me", "/me/profile"})
    public ResponseEntity<UserProfileResponseDto> getMyProfile(Authentication authentication) {

        System.out.println("===== PROFILE 조회 =====");
        System.out.println("authentication = " + authentication);

        String email = authentication.getName();

        System.out.println("email = " + email);

        UserProfileResponseDto responseDto = userService.getUserProfile(email);

        System.out.println("responseDto = " + responseDto);

        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/me/profile")
    public ResponseEntity<?> updateProfile(
            Authentication authentication,
            @RequestBody UserProfileUpdateRequest requestDto
    ) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            String email = authentication.getName();

            userService.updateUserProfile(email, requestDto);

            UserProfileResponseDto responseDto = userService.getUserProfile(email);

            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {
            System.err.println("====== [에러] 프로필 업데이트 중 문제 발생 ======");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버 내부 에러가 발생했습니다.");
        }
    }
}