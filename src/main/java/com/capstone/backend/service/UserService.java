package com.capstone.backend.service;

import com.capstone.backend.dto.UserProfileResponseDto;
import com.capstone.backend.dto.UserProfileUpdateRequest;
import com.capstone.backend.entity.TechStack;
import com.capstone.backend.entity.User;
import com.capstone.backend.entity.UserTechStack;
import com.capstone.backend.repository.TechStackRepository;
import com.capstone.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TechStackRepository techStackRepository;

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
                .region(user.getRegion())
                .techStacks(techStackNames)
                .build();
    }

    @Transactional
    public User updateUserProfile(String email, UserProfileUpdateRequest requestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        List<UserTechStack> newStacks = null;

        if (requestDto.getTechStacks() != null) {
            newStacks = requestDto.getTechStacks().stream()
                    .map(tagName -> {
                        TechStack techStack = techStackRepository.findByName(tagName)
                                .orElseGet(() -> techStackRepository.save(
                                        TechStack.builder()
                                                .name(tagName)
                                                .createdAt(LocalDateTime.now())
                                                .updatedAt(LocalDateTime.now())
                                                .build()
                                ));

                        return UserTechStack.builder()
                                .techStack(techStack)
                                .build();
                    })
                    .collect(Collectors.toList());
        }

        user.updateProfile(
                requestDto.getName(),
                requestDto.getNickname(),
                requestDto.getJobRole(),
                requestDto.getOrganization(),
                requestDto.getRegion(),
                requestDto.getIntroduction(),
                newStacks,
                requestDto.getProfileImg()
        );

        return user;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));
    }
}