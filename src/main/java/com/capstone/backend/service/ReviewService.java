package com.capstone.backend.service;

import com.capstone.backend.dto.ReviewBulkSaveRequestDto;
import com.capstone.backend.dto.ReviewResponseDto;
import com.capstone.backend.entity.Project;
import com.capstone.backend.entity.Review;
import com.capstone.backend.entity.User;
import com.capstone.backend.global.exception.ConflictException;
import com.capstone.backend.global.exception.UnauthorizedException;
import com.capstone.backend.repository.ProjectRepository;
import com.capstone.backend.repository.ReviewRepository;
import com.capstone.backend.repository.ApplicationRepository;
import com.capstone.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ApplicationRepository applicationRepository;

    public List<ReviewResponseDto> getMyReviewsForProject(Long projectId, String reviewerEmail, Long requestedReviewerId) {
        User reviewer = userRepository.findByEmail(reviewerEmail)
                .orElseThrow(() -> new UnauthorizedException("인증된 사용자를 찾을 수 없습니다."));
        validateRequestedUserId(requestedReviewerId, reviewer.getId());

        return reviewRepository.findAllByProjectIdAndReviewerIdOrderByCreatedAtAsc(projectId, reviewer.getId())
                .stream()
                .map(ReviewResponseDto::from)
                .toList();
    }

    public List<ReviewResponseDto> getReceivedReviews(String userEmail, Long requestedUserId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UnauthorizedException("인증된 사용자를 찾을 수 없습니다."));
        validateRequestedUserId(requestedUserId, user.getId());

        return reviewRepository.findAllByRevieweeIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(ReviewResponseDto::from)
                .toList();
    }

    @Transactional
    public void saveBulkReviews(List<ReviewBulkSaveRequestDto> requestDtos, String reviewerEmail) {
        if (requestDtos == null || requestDtos.isEmpty()) {
            throw new IllegalArgumentException("저장할 리뷰가 없습니다.");
        }

        User authenticatedReviewer = userRepository.findByEmail(reviewerEmail)
                .orElseThrow(() -> new UnauthorizedException("인증된 사용자를 찾을 수 없습니다."));

        validateBulkRequestConsistency(requestDtos, authenticatedReviewer.getId());

        ReviewBulkSaveRequestDto firstRequest = requestDtos.get(0);
        Long projectId = firstRequest.getProjectId();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다."));
        validateProjectIsCompleted(project);

        for (ReviewBulkSaveRequestDto dto : requestDtos) {
            User reviewee = userRepository.findById(dto.getRevieweeId())
                    .orElseThrow(() -> new IllegalArgumentException("대상자를 찾을 수 없습니다."));

            validateParticipants(project, authenticatedReviewer.getId(), reviewee.getId());

            reviewRepository.findByProjectIdAndReviewerIdAndRevieweeId(projectId, authenticatedReviewer.getId(), reviewee.getId())
                    .ifPresentOrElse(
                            existingReview -> existingReview.update(dto.getRating(), dto.getComment()),
                            () -> reviewRepository.save(
                                    Review.builder()
                                            .project(project)
                                            .reviewer(authenticatedReviewer)
                                            .reviewee(reviewee)
                                            .rating(dto.getRating())
                                            .comment(dto.getComment())
                                            .build()
                            )
                    );
        }
    }

    private void validateBulkRequestConsistency(List<ReviewBulkSaveRequestDto> requestDtos, Long authenticatedReviewerId) {
        ReviewBulkSaveRequestDto firstRequest = requestDtos.get(0);
        Long projectId = firstRequest.getProjectId();

        for (ReviewBulkSaveRequestDto dto : requestDtos) {
            if (!projectId.equals(dto.getProjectId())) {
                throw new IllegalArgumentException("한 번의 요청에는 동일한 프로젝트의 리뷰만 저장할 수 있습니다.");
            }
            if (dto.getReviewerId() != null && !authenticatedReviewerId.equals(dto.getReviewerId())) {
                throw new UnauthorizedException("reviewer_id가 인증된 사용자와 일치하지 않습니다.");
            }
            if (authenticatedReviewerId.equals(dto.getRevieweeId())) {
                throw new ConflictException("자기 자신에게 리뷰를 남길 수 없습니다.");
            }
        }
    }

    private void validateRequestedUserId(Long requestedUserId, Long authenticatedUserId) {
        if (requestedUserId != null && !requestedUserId.equals(authenticatedUserId)) {
            throw new UnauthorizedException("다른 사용자의 리뷰에는 접근할 수 없습니다.");
        }
    }

    private void validateProjectIsCompleted(Project project) {
        String status = project.getStatus();
        if (!"종료됨".equals(status) && !"COMPLETED".equalsIgnoreCase(status)) {
            throw new ConflictException("프로젝트가 종료된 뒤에만 리뷰를 남길 수 있습니다.");
        }
    }

    private void validateParticipants(Project project, Long reviewerId, Long revieweeId) {
        if (!isProjectParticipant(project, reviewerId)) {
            throw new ConflictException("프로젝트 참여자만 리뷰를 작성할 수 있습니다.");
        }
        if (!isProjectParticipant(project, revieweeId)) {
            throw new ConflictException("프로젝트 참여자에게만 리뷰를 남길 수 있습니다.");
        }
    }

    private boolean isProjectParticipant(Project project, Long userId) {
        if (project.getLeader().getId().equals(userId)) {
            return true;
        }
        return applicationRepository.existsByProjectIdAndApplicantIdAndStatus(project.getId(), userId, "ACCEPTED");
    }
}
