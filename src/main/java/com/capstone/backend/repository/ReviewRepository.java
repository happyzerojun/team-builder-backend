package com.capstone.backend.repository;

import com.capstone.backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @EntityGraph(attributePaths = {"project", "reviewer", "reviewee"})
    List<Review> findAllByProjectIdAndReviewerIdOrderByCreatedAtAsc(Long projectId, Long reviewerId);

    @EntityGraph(attributePaths = {"project", "reviewer", "reviewee"})
    List<Review> findAllByRevieweeIdOrderByCreatedAtDesc(Long revieweeId);

    Optional<Review> findByProjectIdAndReviewerIdAndRevieweeId(Long projectId, Long reviewerId, Long revieweeId);
}
