package com.capstone.backend.service;

import com.capstone.backend.dto.UserProfileResponseDto;
import com.capstone.backend.entity.TechStack;
import com.capstone.backend.entity.User;
import com.capstone.backend.entity.UserTechStack;
import com.capstone.backend.repository.TechStackRepository;
import com.capstone.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TechStackRepository techStackRepository;

    // --- 프로필 조회 (DTO 변환) ---
    @Transactional(readOnly = true)
    public UserProfileResponseDto getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        List<String> techStackNames = user.getUserTechStacks().stream()
                .map(userTechStack -> userTechStack.getTechStack().getName())
                .collect(Collectors.toList());

        return UserProfileResponseDto.builder()
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .jobRole(user.getJobRole())
                .organization(user.getOrganization())
                .introduction(user.getIntroduction())
                .profileImg(user.getProfileImg())
                .techStacks(techStackNames)
                .build();
    }

    // --- 프로필 수정 (새로운 스택 매핑) ---
    @Transactional
    public User updateUserProfile(String email, UserProfileResponseDto requestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        // 문자열 리스트를 UserTechStack 엔티티 리스트로 변환
        List<UserTechStack> newStacks = null;
        if (requestDto.getTechStacks() != null) {
            newStacks = requestDto.getTechStacks().stream()
                    .map(tagName -> {
                        TechStack techStack = techStackRepository.findByName(tagName)
                                .orElseGet(() -> techStackRepository.save(
                                        TechStack.builder().name(tagName).build()
                                ));
                        return UserTechStack.builder()
                                .techStack(techStack)
                                // user는 User.updateProfile() 내부에서 세팅됨
                                .build();
                    })
                    .collect(Collectors.toList());
        }

        // 업데이트 수행
        user.updateProfile(
                requestDto.getName(),
                requestDto.getNickname(),
                requestDto.getJobRole(),
                requestDto.getOrganization(),
                requestDto.getIntroduction(),
                newStacks,
                requestDto.getProfileImg()
        );

        return user; // 컨트롤러에서는 저장된 결과를 DTO로 다시 바꿔서 응답하면 완벽합니다!
    }
}