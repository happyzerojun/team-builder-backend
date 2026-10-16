package com.capstone.backend.repository;

import com.capstone.backend.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * 목록 조회: 응답에 작성자 이름이 들어가므로 leader(N:1)를 함께 가져온다.
     * 컬렉션(기술 스택·포지션)은 fetch join하면 페이징이 메모리에서 일어나므로(HHH90003004)
     * default_batch_fetch_size로 묶어서 가져온다 (TS-13)
     */
    @Override
    @EntityGraph(attributePaths = "leader")
    Page<Project> findAll(Pageable pageable);

    /**
     * 내가 작성한 모집글(마이페이지). leader_id는 FK라 인덱스가 있어 작성자 글만 읽는다.
     * 이전에는 프론트가 전체 목록 첫 페이지만 받아 leader_id로 걸러, 10번째 이후 글이 보이지 않았다 (TS-17)
     */
    @EntityGraph(attributePaths = "leader")
    Page<Project> findByLeaderId(Long leaderId, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Project p " +
            "JOIN p.projectTechStacks pts " +
            "WHERE pts.techStack.id = :techStackId")
    Page<Project> findByTechStackId(@Param("techStackId") Long techStackId, Pageable pageable);

}