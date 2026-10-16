package com.capstone.backend.integration;

import com.capstone.backend.dto.ReviewBulkSaveRequestDto;
import com.capstone.backend.entity.Application;
import com.capstone.backend.entity.Project;
import com.capstone.backend.entity.ProjectStatus;
import com.capstone.backend.entity.Review;
import com.capstone.backend.entity.User;
import com.capstone.backend.global.exception.ConflictException;
import com.capstone.backend.repository.ApplicationRepository;
import com.capstone.backend.repository.ReviewRepository;
import com.capstone.backend.service.ReviewService;
import com.capstone.backend.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** TS-16: 실제 MySQL에서 같은 리뷰를 동시에 저장해도 1건만 남는지 */
@DisplayName("리뷰 중복 방지 동시성 (MySQL)")
class ReviewConcurrencyIntegrationTest extends IntegrationTestSupport {

    private static final int REQUESTS = 6;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    private final List<Long> projectIds = new ArrayList<>();

    @Override
    protected void cleanUpBeforeProjects() {
        projectIds.forEach(reviewRepository::deleteAllByProjectId);
        projectIds.clear();
    }

    /** 종료된 프로젝트 + 팀장·팀원 둘 다 ACCEPTED */
    private Project completedProjectWith(User leader, User member) {
        Project project = projectRepository.save(Project.builder()
                .title("리뷰 동시성 테스트")
                .content("통합 테스트용 모집글")
                .region("온라인")
                .term("3")
                .status(ProjectStatus.COMPLETED.label())
                .leader(leader)
                .build());
        trackProject(project.getId());
        projectIds.add(project.getId());
        for (User user : List.of(leader, member)) {
            applicationRepository.save(Application.builder()
                    .project(project).applicant(user).supportRole("팀원").status("ACCEPTED").build());
        }
        return project;
    }

    private static ReviewBulkSaveRequestDto review(Long projectId, Long revieweeId) {
        ReviewBulkSaveRequestDto dto = new ReviewBulkSaveRequestDto();
        ReflectionTestUtils.setField(dto, "projectId", projectId);
        ReflectionTestUtils.setField(dto, "revieweeId", revieweeId);
        ReflectionTestUtils.setField(dto, "rating", 5);
        ReflectionTestUtils.setField(dto, "comment", "동시 요청");
        return dto;
    }

    @Test
    @DisplayName("같은 리뷰를 동시에 6건 저장하면 1건만 성공하고 나머지는 409, DB에도 1건")
    void concurrentDuplicateReviewsKeepOnlyOne() throws Exception {
        User leader = newUser("rv-leader");
        User member = newUser("rv-member");
        Project project = completedProjectWith(leader, member);

        ExecutorService pool = Executors.newFixedThreadPool(REQUESTS);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<String>> futures = new ArrayList<>();
        for (int i = 0; i < REQUESTS; i++) {
            Callable<String> save = () -> {
                start.await();
                try {
                    reviewService.saveBulkReviews(member.getEmail(), List.of(review(project.getId(), leader.getId())));
                    return "OK";
                } catch (ConflictException e) {
                    return "CONFLICT";
                } catch (Exception e) {
                    return "UNEXPECTED: " + e.getClass().getSimpleName() + " " + e.getMessage();
                }
            };
            futures.add(pool.submit(save));
        }
        start.countDown();
        List<String> results = new ArrayList<>();
        for (Future<String> f : futures) {
            results.add(f.get(30, TimeUnit.SECONDS));
        }
        pool.shutdown();

        assertThat(results).containsOnlyOnce("OK");
        assertThat(results).filteredOn("CONFLICT"::equals).hasSize(REQUESTS - 1);
        assertThat(reviewRepository.findAllByProjectIdAndReviewerId(project.getId(), member.getId())).hasSize(1);
    }

    @Test
    @DisplayName("조회 검사를 거치지 않는 저장도 DB 유니크 제약이 막는다")
    void uniqueConstraintExistsInSchema() {
        User leader = newUser("rv2-leader");
        User member = newUser("rv2-member");
        Project project = completedProjectWith(leader, member);
        reviewRepository.saveAndFlush(Review.builder()
                .project(project).reviewer(member).reviewee(leader).rating(4).build());

        assertThatThrownBy(() -> reviewRepository.saveAndFlush(Review.builder()
                .project(project).reviewer(member).reviewee(leader).rating(1).build()))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining(Review.UNIQUE_REVIEWER_REVIEWEE_PROJECT);
    }

    @Test
    @DisplayName("API로 이미 남긴 리뷰를 다시 저장하면 409 CONFLICT")
    void duplicateViaApiIsConflict() throws Exception {
        User leader = newUser("rv3-leader");
        User member = newUser("rv3-member");
        Project project = completedProjectWith(leader, member);
        String body = """
                [{"project_id": %d, "reviewee_id": %d, "rating": 5, "comment": "좋아요"}]
                """.formatted(project.getId(), leader.getId());

        mockMvc.perform(post("/api/review/bulk").header(HttpHeaders.AUTHORIZATION, bearer(member))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/review/bulk").header(HttpHeaders.AUTHORIZATION, bearer(member))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }
}
