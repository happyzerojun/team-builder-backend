package com.capstone.backend.repository;

import com.capstone.backend.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
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

    Optional<Application> findByProjectIdAndApplicantIdAndStatus(Long projectId, Long applicantId, String status);
}