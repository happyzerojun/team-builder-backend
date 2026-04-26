package com.capstone.backend.service;

import com.capstone.backend.dto.UserProfileUpdateRequestDto;
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
    private final TechStackRepository techStackRepository; // 기술 스택 마스터 테이블 리포지토리

    /**
     * 이메일로 유저 정보를 조회하는 메서드 (조회 전용)
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        // userRepository에 findByEmail이 정의되어 있어야 합니다.
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일을 가진 유저가 없습니다: " + email));
    }

    @Transactional
    public User updateUserProfile(String email, UserProfileUpdateRequestDto requestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        // 🚨 [핵심 로직] 문자열을 TechStack 엔티티로 변환
        List<UserTechStack> newStacks = requestDto.getTags().stream()
                .map(tagName -> {
                    // 1. DB에 해당 이름의 기술 스택이 있는지 확인
                    TechStack techStack = techStackRepository.findByName(tagName)
                            .orElseGet(() -> techStackRepository.save(
                                    TechStack.builder().name(tagName).build()
                            )); // 2. 없으면 새로 생성해서 저장

                    // 3. User와 TechStack을 연결하는 중간 엔티티 생성
                    return UserTechStack.builder()
                            .user(user)
                            .techStack(techStack)
                            .build();
                })
                .collect(Collectors.toList());

        // 4. 업데이트 메서드 호출
        user.updateProfile(
                requestDto.getName(),
                requestDto.getNickname(),
                requestDto.getJobRole(),
                requestDto.getOrganization(),
                requestDto.getIntroduction(),
                newStacks, // 변환된 엔티티 리스트 전달
                requestDto.getProfileImg()
        );

        return user;
    }
}