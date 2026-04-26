package com.capstone.backend.dto;

import lombok.Getter;

@Getter
public class ProjectRequestDto {
    private String title;
    private String content;
    private String region;
    private String status;
    private Long leader_id;
    private Integer term;
}