package com.capstone.backend.service;

import com.capstone.backend.dto.ReviewBulkSaveRequestDto;
import com.capstone.backend.dto.ReviewResponseDto;
import com.capstone.backend.entity.Project;
import com.capstone.backend.entity.Review;
import com.capstone.backend.entity.User;
import com.capstone.backend.repository.ProjectRepository;
import com.capstone.backend.repository.ReviewRepository;
import com.capstone.backend.repository.UserRepository;
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

    // 1. 프로젝트에서 내가 쓴 리뷰 조회
    public List<ReviewResponseDto> getMyReviewsForProject(Long projectId, Long reviewerId) {
        return reviewRepository.findAllByProjectIdAndReviewerId(projectId, reviewerId)
                .stream()
                .map(ReviewResponseDto::from)
                .collect(Collectors.toList());
    }

    // 2. 받은 리뷰 조회
    public List<ReviewResponseDto> getReceivedReviews(Long userId) {
        return reviewRepository.findAllByRevieweeId(userId)
                .stream()
                .map(ReviewResponseDto::from)
                .collect(Collectors.toList());
    }

    // 3. 다중 리뷰 저장
    @Transactional
    public void saveBulkReviews(List<ReviewBulkSaveRequestDto> requestDtos) {
        if (requestDtos == null || requestDtos.isEmpty()) return;

        // 하나의 프로젝트 ID는 동일할 것이므로 첫 번째 값으로 가져옵니다
        Long projectId = requestDtos.get(0).getProjectId();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다."));

        for (ReviewBulkSaveRequestDto dto : requestDtos) {
            User reviewer = userRepository.findById(dto.getReviewerId())
                    .orElseThrow(() -> new IllegalArgumentException("작성자를 찾을 수 없습니다."));
            User reviewee = userRepository.findById(dto.getRevieweeId())
                    .orElseThrow(() -> new IllegalArgumentException("대상자를 찾을 수 없습니다."));

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
}