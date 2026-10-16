package com.capstone.backend.service;

import com.capstone.backend.dto.ReviewBulkSaveRequestDto;
import com.capstone.backend.dto.ReviewResponseDto;
import com.capstone.backend.entity.Application;
import com.capstone.backend.entity.Project;
import com.capstone.backend.entity.ProjectStatus;
import com.capstone.backend.entity.Review;
import com.capstone.backend.entity.User;
import com.capstone.backend.global.exception.ConflictException;
import com.capstone.backend.global.exception.ForbiddenException;
import com.capstone.backend.global.exception.NotFoundException;
import com.capstone.backend.global.exception.UnauthorizedException;
import com.capstone.backend.repository.ApplicationRepository;
import com.capstone.backend.repository.ProjectRepository;
import com.capstone.backend.repository.ReviewRepository;
import com.capstone.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 팀원 상호 평가. 작성자는 토큰의 사용자(본문 reviewer_id 무시),
 * 작성자·대상자 모두 해당 프로젝트의 팀원이어야 하고 종료된 프로젝트에서만 작성할 수 있다 (TS-05)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ApplicationRepository applicationRepository;

    // 1. 프로젝트에서 내가 쓴 리뷰 조회
    public List<ReviewResponseDto> getMyReviewsForProject(String email, Long projectId) {
        User me = currentUser(email);
        return reviewRepository.findAllByProjectIdAndReviewerId(projectId, me.getId())
                .stream()
                .map(ReviewResponseDto::from)
                .collect(Collectors.toList());
    }

    // 1-1. 기존 경로 호환: reviewerId가 본인일 때만
    public List<ReviewResponseDto> getMyReviewsForProject(String email, Long projectId, Long reviewerId) {
        requireSelf(email, reviewerId);
        return getMyReviewsForProject(email, projectId);
    }

    // 2. 내가 받은 리뷰 조회
    public List<ReviewResponseDto> getReceivedReviews(String email) {
        User me = currentUser(email);
        return reviewRepository.findAllByRevieweeId(me.getId())
                .stream()
                .map(ReviewResponseDto::from)
                .collect(Collectors.toList());
    }

    // 2-1. 기존 경로 호환: userId가 본인일 때만
    public List<ReviewResponseDto> getReceivedReviews(String email, Long userId) {
        requireSelf(email, userId);
        return getReceivedReviews(email);
    }

    // 3. 다중 리뷰 저장
    @Transactional
    public void saveBulkReviews(String email, List<ReviewBulkSaveRequestDto> requestDtos) {
        if (requestDtos == null || requestDtos.isEmpty()) return;
        User reviewer = currentUser(email);

        Long projectId = requestDtos.get(0).getProjectId();
        if (projectId == null || requestDtos.stream().anyMatch(d -> !Objects.equals(d.getProjectId(), projectId))) {
            throw new IllegalArgumentException("한 번에 한 프로젝트의 리뷰만 저장할 수 있습니다.");
        }
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 프로젝트입니다."));
        if (project.getProjectStatus() != ProjectStatus.COMPLETED) {
            throw new ConflictException("종료된 프로젝트에서만 리뷰를 남길 수 있습니다.");
        }

        Map<Long, User> members = applicationRepository.findByProjectIdAndStatus(projectId, ApplicationService.ACCEPTED)
                .stream()
                .map(Application::getApplicant)
                .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));
        if (!members.containsKey(reviewer.getId())) {
            throw new ForbiddenException("프로젝트 팀원만 리뷰를 남길 수 있습니다.");
        }

        Set<Long> seen = new HashSet<>();
        for (ReviewBulkSaveRequestDto dto : requestDtos) {
            Long revieweeId = dto.getRevieweeId();
            User reviewee = members.get(revieweeId);
            if (reviewee == null) {
                throw new IllegalArgumentException("리뷰 대상은 같은 프로젝트의 팀원이어야 합니다.");
            }
            if (Objects.equals(revieweeId, reviewer.getId())) {
                throw new IllegalArgumentException("자기 자신은 평가할 수 없습니다.");
            }
            if (dto.getRating() == null || dto.getRating() < 1 || dto.getRating() > 5) {
                throw new IllegalArgumentException("별점은 1~5점이어야 합니다.");
            }
            if (!seen.add(revieweeId)
                    || reviewRepository.existsByProjectIdAndReviewerIdAndRevieweeId(projectId, reviewer.getId(), revieweeId)) {
                throw new ConflictException("이미 리뷰를 남긴 팀원이 있습니다.");
            }

            reviewRepository.save(Review.builder()
                    .project(project)
                    .reviewer(reviewer)
                    .reviewee(reviewee)
                    .rating(dto.getRating())
                    .comment(dto.getComment())
                    .build());
        }
    }

    private void requireSelf(String email, Long userId) {
        if (!Objects.equals(currentUser(email).getId(), userId)) {
            throw new ForbiddenException("다른 사용자의 리뷰 정보는 조회할 수 없습니다.");
        }
    }

    private User currentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("로그인 정보를 찾을 수 없습니다. 다시 로그인해주세요."));
    }
}
