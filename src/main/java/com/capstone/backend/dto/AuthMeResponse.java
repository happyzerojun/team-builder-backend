package com.capstone.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthMeResponse(
        @JsonProperty("user_id")
        Long userId,
        String email,
        String name
) {
}
