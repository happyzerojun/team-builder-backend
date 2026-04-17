package com.capstone.backend.dto;

import com.capstone.backend.entity.TechStack;
import lombok.Getter;

@Getter
public class TechStackResponseDto {
    private Long id;
    private String name;
    private String imageUrl;

    // 엔티티를 받아서 DTO로 쏙 변환해 주는 마법의 생성자
    public TechStackResponseDto(TechStack techStack) {
        this.id = techStack.getId();
        this.name = techStack.getName();
        this.imageUrl = techStack.getImageUrl();
    }
}