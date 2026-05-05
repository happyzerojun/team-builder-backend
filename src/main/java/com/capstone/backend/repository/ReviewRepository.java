package com.capstone.backend.repository;

import com.capstone.backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 1. 특정 프로젝트에서 내가 작성한 리뷰 목록 찾기
    List<Review> findAllByProjectIdAndReviewerId(Long projectId, Long reviewerId);

    // 2. 내가 받은 모든 리뷰 찾기 (마이페이지용)
    List<Review> findAllByRevieweeId(Long revieweeId);
}