package com.capstone.backend.controller;

import com.capstone.backend.dto.UserProfileResponseDto;
import com.capstone.backend.dto.UserProfileUpdateRequest;
import com.capstone.backend.entity.User;
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
    public ResponseEntity<?> updateProfile(
            Authentication authentication,
            @RequestBody UserProfileUpdateRequest requestDto // 🚨 [수정 1] 방금 만든 요청 전용 DTO로 받기!
    ) {
        System.out.println("====== [도착] 프론트에서 프로필 수정 요청이 들어왔습니다! ======");
        System.out.println("수정할 닉네임: " + requestDto.getNickname());
        System.out.println("수정할 기술스택: " + requestDto.getTechStacks());

        try {
            // 1. JWT 토큰에서 현재 로그인한 유저 이메일 추출
            String email = authentication.getName();

            // 2. 서비스 로직 실행 (DB 업데이트)
            // 🚨 주의: UserService의 updateUserProfile 메서드도 파라미터를 UserProfileUpdateRequest로 바꿔주셔야 합니다!
            User updatedUser = userService.updateUserProfile(email, requestDto);

            // 3. 🚨 [수정 2] 무한 루프 방지! Entity(User)를 그대로 던지지 않고 Response DTO로 포장해서 반환
            UserProfileResponseDto responseDto = UserProfileResponseDto.builder()
                    .email(updatedUser.getEmail())
                    .name(updatedUser.getName())
                    .nickname(updatedUser.getNickname())
                    .jobRole(updatedUser.getJobRole())
                    .organization(updatedUser.getOrganization())
                    .introduction(updatedUser.getIntroduction())
                    .profileImg(updatedUser.getProfileImg())
                    // 주의: techStacks는 서비스 레이어나 여기서 String 리스트로 변환해서 넣어주세요!
                    .build();

            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {
            System.err.println("====== [에러] 프로필 업데이트 중 문제 발생! ======");
            e.printStackTrace(); // 콘솔에 빨간 에러의 정체를 명확히 찍어줍니다.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 에러가 발생했습니다.");
        }
    }
}