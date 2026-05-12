package com.capstone.backend.service;

import com.capstone.backend.dto.ReviewBulkSaveRequestDto;
import com.capstone.backend.entity.Project;
import com.capstone.backend.entity.Review;
import com.capstone.backend.entity.User;
import com.capstone.backend.global.exception.ConflictException;
import com.capstone.backend.repository.ApplicationRepository;
import com.capstone.backend.repository.ProjectRepository;
import com.capstone.backend.repository.ReviewRepository;
import com.capstone.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Test
    void saveBulkReviewsCreatesNewReviewForCompletedProject() {
        ReviewService reviewService = new ReviewService(
                reviewRepository,
                userRepository,
                projectRepository,
                applicationRepository
        );

        User leader = user(1L, "leader", "팀장");
        User member = user(2L, "member", "팀원");
        Project project = project(10L, "종료됨", leader);
        ReviewBulkSaveRequestDto request = request(10L, 1L, 2L, 5, "좋았어요");

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(userRepository.findByEmail("leader@example.com")).thenReturn(Optional.of(leader));
        when(userRepository.findById(2L)).thenReturn(Optional.of(member));
        when(applicationRepository.existsByProjectIdAndApplicantIdAndStatus(10L, 2L, "ACCEPTED")).thenReturn(true);
        when(reviewRepository.findByProjectIdAndReviewerIdAndRevieweeId(10L, 1L, 2L)).thenReturn(Optional.empty());

        reviewService.saveBulkReviews(List.of(request), "leader@example.com");

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        Review savedReview = reviewCaptor.getValue();
        assertEquals(5, savedReview.getRating());
        assertEquals("좋았어요", savedReview.getComment());
        assertEquals(leader, savedReview.getReviewer());
        assertEquals(member, savedReview.getReviewee());
        assertEquals(project, savedReview.getProject());
    }

    @Test
    void saveBulkReviewsUpdatesExistingReviewInsteadOfDuplicating() {
        ReviewService reviewService = new ReviewService(
                reviewRepository,
                userRepository,
                projectRepository,
                applicationRepository
        );

        User leader = user(1L, "leader", "팀장");
        User member = user(2L, "member", "팀원");
        Project project = project(10L, "COMPLETED", leader);
        ReviewBulkSaveRequestDto request = request(10L, 1L, 2L, 4, "수정된 리뷰");

        Review existingReview = Review.builder()
                .project(project)
                .reviewer(leader)
                .reviewee(member)
                .rating(2)
                .comment("예전 리뷰")
                .build();

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(userRepository.findByEmail("leader@example.com")).thenReturn(Optional.of(leader));
        when(userRepository.findById(2L)).thenReturn(Optional.of(member));
        when(applicationRepository.existsByProjectIdAndApplicantIdAndStatus(10L, 2L, "ACCEPTED")).thenReturn(true);
        when(reviewRepository.findByProjectIdAndReviewerIdAndRevieweeId(10L, 1L, 2L)).thenReturn(Optional.of(existingReview));

        reviewService.saveBulkReviews(List.of(request), "leader@example.com");

        assertEquals(4, existingReview.getRating());
        assertEquals("수정된 리뷰", existingReview.getComment());
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void saveBulkReviewsRejectsProjectThatIsNotCompleted() {
        ReviewService reviewService = new ReviewService(
                reviewRepository,
                userRepository,
                projectRepository,
                applicationRepository
        );

        User leader = user(1L, "leader", "팀장");
        Project project = project(10L, "진행중", leader);
        ReviewBulkSaveRequestDto request = request(10L, 1L, 2L, 5, "좋았어요");

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(userRepository.findByEmail("leader@example.com")).thenReturn(Optional.of(leader));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> reviewService.saveBulkReviews(List.of(request), "leader@example.com")
        );

        assertEquals("프로젝트가 종료된 뒤에만 리뷰를 남길 수 있습니다.", exception.getMessage());
    }

    @Test
    void saveBulkReviewsRejectsSelfReview() {
        ReviewService reviewService = new ReviewService(
                reviewRepository,
                userRepository,
                projectRepository,
                applicationRepository
        );

        ReviewBulkSaveRequestDto request = request(10L, 1L, 1L, 5, "셀프 리뷰");
        when(userRepository.findByEmail("leader@example.com")).thenReturn(Optional.of(user(1L, "leader", "팀장")));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> reviewService.saveBulkReviews(List.of(request), "leader@example.com")
        );

        assertEquals("자기 자신에게 리뷰를 남길 수 없습니다.", exception.getMessage());
    }

    @Test
    void saveBulkReviewsRejectsReviewerIdThatDoesNotMatchJwtUser() {
        ReviewService reviewService = new ReviewService(
                reviewRepository,
                userRepository,
                projectRepository,
                applicationRepository
        );

        ReviewBulkSaveRequestDto request = request(10L, 999L, 2L, 5, "위조");
        when(userRepository.findByEmail("leader@example.com")).thenReturn(Optional.of(user(1L, "leader", "팀장")));

        assertThrows(
                com.capstone.backend.global.exception.UnauthorizedException.class,
                () -> reviewService.saveBulkReviews(List.of(request), "leader@example.com")
        );
    }

    @Test
    void getReceivedReviewsRejectsDifferentUserIdAccess() {
        ReviewService reviewService = new ReviewService(
                reviewRepository,
                userRepository,
                projectRepository,
                applicationRepository
        );

        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(user(2L, "member", "팀원")));

        assertThrows(
                com.capstone.backend.global.exception.UnauthorizedException.class,
                () -> reviewService.getReceivedReviews("member@example.com", 3L)
        );
    }

    private ReviewBulkSaveRequestDto request(Long projectId, Long reviewerId, Long revieweeId, Integer rating, String comment) {
        ReviewBulkSaveRequestDto request = new ReviewBulkSaveRequestDto();
        ReflectionTestUtils.setField(request, "projectId", projectId);
        ReflectionTestUtils.setField(request, "reviewerId", reviewerId);
        ReflectionTestUtils.setField(request, "revieweeId", revieweeId);
        ReflectionTestUtils.setField(request, "rating", rating);
        ReflectionTestUtils.setField(request, "comment", comment);
        return request;
    }

    private User user(Long id, String email, String name) {
        User user = User.builder()
                .email(email + "@example.com")
                .password("pw")
                .name(name)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Project project(Long id, String status, User leader) {
        Project project = Project.builder()
                .title("프로젝트")
                .content("내용")
                .region("서울")
                .term("단기")
                .status(status)
                .leader(leader)
                .build();
        ReflectionTestUtils.setField(project, "id", id);
        return project;
    }
}
