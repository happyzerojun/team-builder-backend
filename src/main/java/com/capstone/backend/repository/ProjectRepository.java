package com.capstone.backend.repository;

import com.capstone.backend.entity.Project; // 영준님의 실제 Project 엔티티 경로로 맞춰주세요!
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    // 🔥 JpaRepository를 상속받으면 기본적으로 findAll(), findById(), save() 같은
    // 마법의 메서드들을 스프링이 알아서 다 만들어줍니다. 안은 텅 비워두시면 됩니다!
}