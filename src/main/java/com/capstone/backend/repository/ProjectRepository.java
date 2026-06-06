package com.capstone.backend.repository;

import com.capstone.backend.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT DISTINCT p FROM Project p " +
            "JOIN p.projectTechStacks pts " +
            "WHERE pts.techStack.id = :techStackId")
    Page<Project> findByTechStackId(@Param("techStackId") Long techStackId, Pageable pageable);

}