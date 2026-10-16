package com.capstone.backend.repository;

import com.capstone.backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 1. 특정 프로젝트에서 내가 작성한 리뷰 목록 찾기
    List<Review> findAllByProjectIdAndReviewerId(Long projectId, Long reviewerId);

    // 2. 내가 받은 모든 리뷰 찾기 (마이페이지용)
    List<Review> findAllByRevieweeId(Long revieweeId);

    boolean existsByProjectIdAndReviewerIdAndRevieweeId(Long projectId, Long reviewerId, Long revieweeId);

    // 모집글 삭제 시 FK(project_id) 때문에 먼저 지운다
    @Transactional
    @Modifying
    @Query("delete from Review r where r.project.id = :projectId")
    void deleteAllByProjectId(@Param("projectId") Long projectId);
}