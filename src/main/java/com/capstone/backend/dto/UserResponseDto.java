package com.capstone.backend.dto;

public record UserResponseDto(
    Long user_id,
    String email,
    String name
) {}