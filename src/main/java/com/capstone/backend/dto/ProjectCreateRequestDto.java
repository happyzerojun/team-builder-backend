package com.capstone.backend.dto;

import java.util.List;

public record ProjectCreateRequestDto(
        String title,
        String content,
        String region,
        String term,
        List<Long> techStackIds
) {}