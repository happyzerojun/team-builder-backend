package com.capstone.backend.repository;

import com.capstone.backend.entity.TechStack;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TechStackRepository extends JpaRepository<TechStack, Long> {
    // JpaRepository 상속만 받아도 findAll() 기능이 공짜로 생깁니다!
    // 🚨 [핵심 추가] 글자로 기술 스택을 찾기 위한 메서드!
    // "Java"라고 던지면 DB에서 name 컬럼이 "Java"인 행을 찾아옵니다.
    Optional<TechStack> findByName(String name);
}