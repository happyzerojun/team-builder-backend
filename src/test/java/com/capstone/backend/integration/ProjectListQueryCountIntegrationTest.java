package com.capstone.backend.integration;

import com.capstone.backend.entity.Project;
import com.capstone.backend.entity.ProjectPosition;
import com.capstone.backend.entity.ProjectTechStack;
import com.capstone.backend.entity.TechStack;
import com.capstone.backend.entity.User;
import com.capstone.backend.repository.ProjectPositionRepository;
import com.capstone.backend.repository.ProjectTechStackRepository;
import com.capstone.backend.repository.TechStackRepository;
import com.capstone.backend.support.IntegrationTestSupport;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** TS-13: 모집글 목록 조회의 쿼리 수가 글 수에 비례해 늘지 않는다 */
@DisplayName("모집글 목록 N+1 (MySQL)")
class ProjectListQueryCountIntegrationTest extends IntegrationTestSupport {

    private static final int PROJECTS = 6;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private TechStackRepository techStackRepository;

    @Autowired
    private ProjectTechStackRepository projectTechStackRepository;

    @Autowired
    private ProjectPositionRepository projectPositionRepository;

    @Test
    @DisplayName("작성자가 모두 다른 글 6건 + 기술 스택 2개 + 포지션: 쿼리 수는 상수")
    void listQueryCountIsConstant() throws Exception {
        List<TechStack> stacks = techStackRepository.findAll().stream().limit(2).toList();
        assertThat(stacks).hasSize(2); // DataInitializer가 넣은 기본 스택

        for (int i = 0; i < PROJECTS; i++) {
            User leader = newUser("n1-leader" + i);
            Project project = newProject(leader, "N+1 테스트 " + i);
            for (TechStack stack : stacks) {
                projectTechStackRepository.save(ProjectTechStack.builder().project(project).techStack(stack).build());
            }
            projectPositionRepository.save(ProjectPosition.builder()
                    .project(project).roleName("백엔드").requiredCount(2).currentCount(0).build());
        }

        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        mockMvc.perform(get("/api/projects").param("size", "50"))
                .andExpect(status().isOk());

        // 목록(작성자 포함) 1 + 개수 1 + 기술 스택 연결 1 + 기술 스택 1 + 포지션 1 = 5 안팎.
        // 배치 페치·EntityGraph가 없으면 작성자 6 + 스택 연결 6 + 포지션 6 ... 으로 20건을 넘는다.
        assertThat(statistics.getPrepareStatementCount()).isLessThanOrEqualTo(7);
    }
}
