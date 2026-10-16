package com.capstone.backend.repository;

import com.capstone.backend.entity.Application;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    @Query("SELECT a FROM Application a JOIN FETCH a.applicant JOIN FETCH a.project WHERE a.applicant.id = :applicantId")
    List<Application> findByApplicantId(@Param("applicantId") Long applicantId);

    @Query("SELECT a FROM Application a JOIN FETCH a.applicant JOIN FETCH a.project WHERE a.project.id = :projectId")
    List<Application> findByProjectId(@Param("projectId") Long projectId);

    List<Application> findByProjectIdAndStatus(Long projectId, String status);

    boolean existsByApplicantIdAndProjectId(Long applicantId, Long projectId);

    /**
     * 지원서 행을 SELECT ... FOR UPDATE로 잠근다.
     * 같은 지원서에 대한 수락 요청 두 건(버튼 연타·두 탭)이 둘 다 PENDING을 읽고 정원을 두 번 늘리는 것을 막는다.
     * 연관 엔티티를 fetch join하면 user·project 행까지 잠기므로 지원서 행만 잠그고 나머지는 지연 로딩한다 (TS-06)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Application a WHERE a.id = :id")
    Optional<Application> findByIdForUpdate(@Param("id") Long id);

    Optional<Application> findByApplicantIdAndProjectId(Long applicantId, Long projectId);

    Optional<Application> findByProjectIdAndApplicantIdAndStatus(Long projectId, Long applicantId, String status);
}