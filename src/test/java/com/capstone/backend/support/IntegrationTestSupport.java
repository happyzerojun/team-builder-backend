package com.capstone.backend.support;

import com.capstone.backend.entity.Project;
import com.capstone.backend.entity.ProjectStatus;
import com.capstone.backend.entity.User;
import com.capstone.backend.global.jwt.JwtUtil;
import com.capstone.backend.repository.ProjectRepository;
import com.capstone.backend.repository.UserRepository;
import com.capstone.backend.service.ai.GeminiClient;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 실제 MySQL(CI 서비스 컨테이너)에 붙는 통합 테스트 공통 설정.
 * 모든 하위 클래스가 같은 설정을 써서 Spring 컨텍스트를 한 번만 띄운다.
 * Gemini 호출은 GeminiClient 목으로 대체한다.
 * 테스트가 만든 데이터는 @AfterEach에서 지운다(공용 DB라 다른 테스트에 영향 주지 않게).
 */
@SpringBootTest(properties = {
        "spring.jpa.properties.hibernate.generate_statistics=true",
        "logging.level.org.hibernate.engine.internal.StatisticalLoggingSessionEventListener=WARN"
})
@AutoConfigureMockMvc
public abstract class IntegrationTestSupport {

    @MockBean
    protected GeminiClient geminiClient;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JwtUtil jwtUtil;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ProjectRepository projectRepository;

    private final List<Long> createdProjectIds = new ArrayList<>();
    private final List<Long> createdUserIds = new ArrayList<>();

    protected User newUser(String prefix) {
        User user = userRepository.save(User.builder()
                .email(prefix + "-" + UUID.randomUUID() + "@test.local")
                .password("{noop}unused")
                .name(prefix)
                .build());
        createdUserIds.add(user.getId());
        return user;
    }

    protected Project newProject(User leader, String title) {
        Project project = projectRepository.save(Project.builder()
                .title(title)
                .content("통합 테스트용 모집글")
                .region("온라인")
                .term("3")
                .status(ProjectStatus.RECRUITING.label())
                .leader(leader)
                .build());
        createdProjectIds.add(project.getId());
        return project;
    }

    /** 다른 경로(API 등)로 만든 프로젝트도 정리 대상으로 등록 */
    protected void trackProject(Long projectId) {
        createdProjectIds.add(projectId);
    }

    protected String bearer(User user) {
        return "Bearer " + jwtUtil.createToken(user.getEmail());
    }

    @AfterEach
    void cleanUpCreatedData() {
        cleanUpBeforeProjects();
        createdProjectIds.forEach(projectRepository::deleteById);   // 지원서·포지션·스택 연결은 cascade로 함께 삭제
        createdUserIds.forEach(userRepository::deleteById);
        createdProjectIds.clear();
        createdUserIds.clear();
    }

    /** 프로젝트를 참조하는 데이터(리뷰 등)를 하위 클래스가 먼저 지울 때 사용 */
    protected void cleanUpBeforeProjects() {
    }
}
