package com.capstone.backend.repository;

import com.capstone.backend.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // 유저 ID로 지원 내역 조회 (마이페이지용)
    List<Application> findByApplicantId(Long applicantId);

    // 프로젝트 ID로 지원자 목록 조회 (프로젝트 리더용)
    List<Application> findByProjectId(Long projectId);

    // 중복 지원 방지를 위한 확인 메서드
    boolean existsByApplicantIdAndProjectId(Long applicantId, Long projectId);
}
