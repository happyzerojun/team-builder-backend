package com.capstone.backend.service;

import com.capstone.backend.dto.UserProfileUpdateRequestDto;
import com.capstone.backend.entity.User;
import com.capstone.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void updateUserProfile(String email, UserProfileUpdateRequestDto requestDto) {
        // 1. 이메일로 유저 찾기 (준원님이 UserRepository에 findByEmail 만들어 두셨을 겁니다!)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        // 2. 유저 정보 업데이트
        user.updateProfile(
                requestDto.getExperienceLevel(),
                requestDto.getGithubUrl(),
                requestDto.getBaekjoonId()
        );
        // @Transactional 덕분에 따로 save()를 안 해도 메서드가 끝나면 DB에 자동 반영됩니다. (더티 체킹)
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));
    }
}