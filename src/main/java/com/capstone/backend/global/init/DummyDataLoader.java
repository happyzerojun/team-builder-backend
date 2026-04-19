package com.capstone.backend.global.init;

import com.capstone.backend.entity.Project;
import com.capstone.backend.entity.User;
import com.capstone.backend.repository.ProjectRepository;
import com.capstone.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder; // 🔥 이거 추가!
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DummyDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PasswordEncoder passwordEncoder; // 🔥 시큐리티의 암호화 도구 주입!

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (projectRepository.count() > 0) {
            return;
        }

        // 1. 리더(User) 생성 - 비밀번호 암호화 적용!
        User dummyLeader = User.builder()
                .email("admin@teambuilder.com")
                // 🔥 raw 비밀번호를 넣으면 알아서 Bcrypt 해시값으로 변환해서 DB에 쏙!
                .password(passwordEncoder.encode("dummy_password_123!"))
                .name("테스트리더")
                .experienceLevel("중급")
                .build();

        userRepository.save(dummyLeader);

        // 2. 테스트용 프로젝트 생성
        Project project1 = Project.builder()
                .title("초보 환영! Spring Boot 게시판 만들기")
                .content("백엔드 기초를 다지기 위한 프로젝트입니다. CRUD 위주로 진행합니다.")
                .region("온라인")
                .status("모집중")
                .term("1개월")
                .leader(dummyLeader)
                .build();

        Project project2 = Project.builder()
                .title("React와 Spring으로 쇼핑몰 클론코딩")
                .content("중급자 이상 환영! 결제 모듈 연동 및 도커 배포까지 진행할 예정입니다.")
                .region("광주광역시")
                .status("모집중")
                .term("3개월")
                .leader(dummyLeader)
                .build();

        projectRepository.save(project1);
        projectRepository.save(project2);

        System.out.println("=================================");
        System.out.println("🚀 [보안 적용] 테스트 데이터 자동 주입 완료!");
        System.out.println("=================================");
    }
}