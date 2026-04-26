package com.capstone.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MeResponse {

    @JsonProperty("user_id")
    private final long userId;

    private final String email;
    private final String name;

    public MeResponse(long userId, String email, String name) {
        this.userId = userId;
        this.email = email;
        this.name = name;
    }

    public static MeResponse empty() {
        return new MeResponse(0L, "", "");
    }

    public long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}
