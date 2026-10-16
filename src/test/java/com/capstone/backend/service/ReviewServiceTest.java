package com.capstone.backend.service;

import com.capstone.backend.dto.ReviewBulkSaveRequestDto;
import com.capstone.backend.entity.Application;
import com.capstone.backend.entity.Project;
import com.capstone.backend.entity.Review;
import com.capstone.backend.entity.User;
import com.capstone.backend.global.exception.ConflictException;
import com.capstone.backend.global.exception.ForbiddenException;
import com.capstone.backend.repository.ApplicationRepository;
import com.capstone.backend.repository.ProjectRepository;
import com.capstone.backend.repository.ReviewRepository;
import com.capstone.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** TS-05: 리뷰 작성자는 토큰의 사용자, 작성자·대상 모두 팀원, 종료된 프로젝트만 */
@ExtendWith(MockitoExtension.class)
@DisplayName("팀원 리뷰 권한")
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private ApplicationRepository applicationRepository;

    @InjectMocks
    private ReviewService reviewService;

    private final User leader = User.builder().id(1L).email("leader@example.com").build();
    private final User member = User.builder().id(2L).email("member@example.com").build();
    private final User outsider = User.builder().id(3L).email("out@example.com").build();

    private Project project(String status) {
        return Project.builder().id(10L).status(status).leader(leader).build();
    }

    private List<Application> members(Project p) {
        return List.of(
                Application.builder().project(p).applicant(leader).status("ACCEPTED").build(),
                Application.builder().project(p).applicant(member).status("ACCEPTED").build());
    }

    private static ReviewBulkSaveRequestDto review(Long revieweeId, Long reviewerIdInBody) {
        ReviewBulkSaveRequestDto dto = new ReviewBulkSaveRequestDto();
        ReflectionTestUtils.setField(dto, "projectId", 10L);
        ReflectionTestUtils.setField(dto, "reviewerId", reviewerIdInBody);
        ReflectionTestUtils.setField(dto, "revieweeId", revieweeId);
        ReflectionTestUtils.setField(dto, "rating", 5);
        return dto;
    }

    @Test
    @DisplayName("본문의 reviewer_id를 무시하고 토큰의 사용자로 저장")
    void reviewerIsCurrentUser() {
        Project p = project("종료됨");
        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(member));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(p));
        when(applicationRepository.findByProjectIdAndStatus(10L, "ACCEPTED")).thenReturn(members(p));

        reviewService.saveBulkReviews("member@example.com", List.of(review(1L, 999L)));

        ArgumentCaptor<Review> saved = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(saved.capture());
        assertThat(saved.getValue().getReviewer()).isSameAs(member);
        assertThat(saved.getValue().getReviewee()).isSameAs(leader);
    }

    @Test
    @DisplayName("팀원이 아니면 403, 진행 중인 프로젝트면 409")
    void rejectsOutsiderAndUnfinishedProject() {
        Project done = project("종료됨");
        when(userRepository.findByEmail("out@example.com")).thenReturn(Optional.of(outsider));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(done));
        when(applicationRepository.findByProjectIdAndStatus(10L, "ACCEPTED")).thenReturn(members(done));
        assertThatThrownBy(() -> reviewService.saveBulkReviews("out@example.com", List.of(review(1L, null))))
                .isInstanceOf(ForbiddenException.class);

        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(member));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project("진행중")));
        assertThatThrownBy(() -> reviewService.saveBulkReviews("member@example.com", List.of(review(1L, null))))
                .isInstanceOf(ConflictException.class);

        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("동시 요청으로 유니크 제약(리뷰어·대상·프로젝트)에 걸리면 409 (TS-16)")
    void duplicateKeyBecomesConflict() {
        Project p = project("종료됨");
        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(member));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(p));
        when(applicationRepository.findByProjectIdAndStatus(10L, "ACCEPTED")).thenReturn(members(p));
        // 조회는 통과(다른 트랜잭션이 아직 커밋 전), INSERT에서 MySQL이 중복 키로 거부한 상황
        when(reviewRepository.save(any(Review.class))).thenThrow(new DataIntegrityViolationException("could not execute statement",
                new SQLIntegrityConstraintViolationException(
                        "Duplicate entry '2-1-10' for key 'review." + Review.UNIQUE_REVIEWER_REVIEWEE_PROJECT + "'")));

        assertThatThrownBy(() -> reviewService.saveBulkReviews("member@example.com", List.of(review(1L, null))))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이미 리뷰를 남긴 팀원이 있습니다.");
    }

    @Test
    @DisplayName("리뷰 유니크 제약이 아닌 무결성 위반은 그대로 올린다")
    void otherIntegrityViolationIsRethrown() {
        Project p = project("종료됨");
        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(member));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(p));
        when(applicationRepository.findByProjectIdAndStatus(10L, "ACCEPTED")).thenReturn(members(p));
        when(reviewRepository.save(any(Review.class))).thenThrow(new DataIntegrityViolationException("x",
                new SQLIntegrityConstraintViolationException("Column 'rating' cannot be null")));

        assertThatThrownBy(() -> reviewService.saveBulkReviews("member@example.com", List.of(review(1L, null))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("다른 사람이 받은 리뷰는 조회할 수 없다")
    void cannotReadOthersReceivedReviews() {
        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(member));
        assertThatThrownBy(() -> reviewService.getReceivedReviews("member@example.com", 1L))
                .isInstanceOf(ForbiddenException.class);
    }
}
