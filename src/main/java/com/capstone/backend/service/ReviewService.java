package com.capstone.backend.service;

import com.capstone.backend.dto.ReviewBulkSaveRequestDto;
import com.capstone.backend.dto.ReviewResponseDto;
import com.capstone.backend.entity.Project;
import com.capstone.backend.entity.Review;
import com.capstone.backend.entity.User;
import com.capstone.backend.repository.ProjectRepository;
import com.capstone.backend.repository.ReviewRepository;
import com.capstone.backend.repository.UserRepository;
import com.capstone.backend.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ApplicationRepository applicationRepository;

    // 1. 프로젝트에서 내가 쓴 리뷰 조회
    public List<ReviewResponseDto> getMyReviewsForProject(Long projectId, String email) {
        User reviewer = findUser(email);
        return reviewRepository.findAllByProjectIdAndReviewerId(projectId, reviewer.getId())
                .stream()
                .map(ReviewResponseDto::from)
                .collect(Collectors.toList());
    }

    // 2. 받은 리뷰 조회
    public List<ReviewResponseDto> getReceivedReviews(String email) {
        User user = findUser(email);
        return reviewRepository.findAllByRevieweeId(user.getId())
                .stream()
                .map(ReviewResponseDto::from)
                .collect(Collectors.toList());
    }

    // 3. 다중 리뷰 저장
    @Transactional
    public void saveBulkReviews(List<ReviewBulkSaveRequestDto> requestDtos, String email) {
        if (requestDtos == null || requestDtos.isEmpty()) return;

        // 하나의 프로젝트 ID는 동일할 것이므로 첫 번째 값으로 가져옵니다
        Long projectId = requestDtos.get(0).getProjectId();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다."));
        User reviewer = findUser(email);
        requireProjectMember(project, reviewer);

        for (ReviewBulkSaveRequestDto dto : requestDtos) {
            if (!projectId.equals(dto.getProjectId())) {
                throw new IllegalArgumentException("모든 리뷰는 같은 프로젝트에 속해야 합니다.");
            }
            User reviewee = userRepository.findById(dto.getRevieweeId())
                    .orElseThrow(() -> new IllegalArgumentException("대상자를 찾을 수 없습니다."));
            if (reviewer.getId().equals(reviewee.getId())) {
                throw new IllegalArgumentException("자기 자신을 리뷰할 수 없습니다.");
            }
            requireProjectMember(project, reviewee);
            if (dto.getRating() == null || dto.getRating() < 1 || dto.getRating() > 5) {
                throw new IllegalArgumentException("평점은 1점에서 5점 사이여야 합니다.");
            }

            Review review = Review.builder()
                    .project(project)
                    .reviewer(reviewer)
                    .reviewee(reviewee)
                    .rating(dto.getRating())
                    .comment(dto.getComment())
                    .build();

            reviewRepository.save(review);
        }
    }

    private User findUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private void requireProjectMember(Project project, User user) {
        boolean isLeader = project.getLeader().getId().equals(user.getId());
        boolean isMember = applicationRepository.existsByApplicantIdAndProjectIdAndStatus(
                user.getId(), project.getId(), "ACCEPTED");
        if (!isLeader && !isMember) {
            throw new IllegalArgumentException("프로젝트 팀원만 리뷰할 수 있습니다.");
        }
    }
}
