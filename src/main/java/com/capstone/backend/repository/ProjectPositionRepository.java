package com.capstone.backend.repository;

import com.capstone.backend.entity.ProjectPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProjectPositionRepository extends JpaRepository<ProjectPosition, Long> {

    Optional<ProjectPosition> findByProjectIdAndRoleName(Long projectId, String roleName);

    List<ProjectPosition> findByProjectId(Long projectId);

    boolean existsByProjectId(Long projectId);

    /**
     * 모집글 수정: 모집 인원 변경. 현재 합류 인원 이상으로만 바꿀 수 있다 (TS-19)
     * 엔티티 필드를 바꿔 dirty checking으로 저장하면 UPDATE에 읽어 둔 current_count까지 함께 써서,
     * 그 사이 커밋된 수락(+1)을 덮어쓸 수 있다(lost update). 그래서 required_count만 조건부로 바꾼다.
     */
    @Modifying(flushAutomatically = true)
    @Query("update ProjectPosition p set p.requiredCount = :requiredCount, p.updatedAt = :now "
            + "where p.id = :id and coalesce(p.currentCount, 0) <= :requiredCount")
    int updateRequiredCountIfFits(@Param("id") Long id, @Param("requiredCount") int requiredCount,
                                  @Param("now") LocalDateTime now);

    /** 모집글 수정: 합류한 팀원이 없는 포지션만 삭제. 영향 행 0 = 그 사이 누군가 합류함 (TS-19) */
    @Modifying(flushAutomatically = true)
    @Query("delete from ProjectPosition p where p.id = :id and coalesce(p.currentCount, 0) = 0")
    int deleteIfEmpty(@Param("id") Long id);

    /**
     * 정원이 남아 있을 때만 +1. 영향 행 수 0 = 정원 초과.
     *
     * 엔티티에서 "읽고 비교하고 쓰는" 방식(increaseCurrentCount)은 두 트랜잭션이 같은 값(예: 2/3)을 읽고
     * 둘 다 3으로 써서 정원을 넘길 수 있다(lost update). 조건부 UPDATE는 InnoDB가 행 잠금을 잡은 뒤
     * 최신 커밋 값으로 WHERE를 다시 평가하므로 동시 수락에서도 정원을 넘지 않는다 (TS-06)
     *
     * flushAutomatically: 같은 트랜잭션에서 바꾼 지원서 상태를 먼저 flush해, 여기서 실패(0행)하면 함께 롤백되게 한다.
     */
    @Transactional
    @Modifying(flushAutomatically = true)
    @Query("update ProjectPosition p set p.currentCount = coalesce(p.currentCount, 0) + 1, p.updatedAt = :now "
            + "where p.id = :id and (p.requiredCount is null or coalesce(p.currentCount, 0) < p.requiredCount)")
    int increaseIfAvailable(@Param("id") Long id, @Param("now") LocalDateTime now);

    /** 수락된 팀원이 빠질 때 -1 (0 밑으로 내려가지 않음) */
    @Transactional
    @Modifying(flushAutomatically = true)
    @Query("update ProjectPosition p set p.currentCount = p.currentCount - 1, p.updatedAt = :now "
            + "where p.id = :id and p.currentCount > 0")
    int decreaseIfPositive(@Param("id") Long id, @Param("now") LocalDateTime now);
}
