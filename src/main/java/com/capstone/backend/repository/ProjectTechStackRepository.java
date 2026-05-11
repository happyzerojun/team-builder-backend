package com.capstone.backend.repository;

import com.capstone.backend.entity.ProjectTechStack;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectTechStackRepository extends JpaRepository<ProjectTechStack, Long> {
}