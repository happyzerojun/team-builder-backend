package com.capstone.backend.controller;

import com.capstone.backend.dto.UserProfileResponseDto;
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
    public ResponseEntity<UserProfileResponseDto> getMyProfile(Authentication authentication) {
        String email = authentication.getName();

        // 엔티티가 아닌 DTO를 받아서 프론트에 넘깁니다.
        // 잭슨(Jackson)은 더 이상 무한 루프에 빠지지 않고 이 DTO만 예쁘게 JSON으로 만듭니다.
        UserProfileResponseDto responseDto = userService.getUserProfile(email);

        return ResponseEntity.ok(responseDto);
    }



    @PutMapping("/me/profile")
    public ResponseEntity<?> updateProfile(Authentication authentication, @RequestBody UserProfileResponseDto requestDto) {
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