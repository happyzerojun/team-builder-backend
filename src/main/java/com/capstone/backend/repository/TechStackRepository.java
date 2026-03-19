package com.capstone.backend.repository;

import com.capstone.backend.entity.TechStack;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechStackRepository extends JpaRepository<TechStack, Long> {
    // JpaRepository 상속만 받아도 findAll() 기능이 공짜로 생깁니다!
}