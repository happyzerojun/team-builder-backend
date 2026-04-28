package com.capstone.backend.repository;

import com.capstone.backend.entity.TechStack;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TechStackRepository extends JpaRepository<TechStack, Long> {
    Optional<TechStack> findByName(String name);
}