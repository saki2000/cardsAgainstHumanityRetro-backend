package com.retro.retro_against_humanity_backend.dto;

import lombok.Data;

@Data
public class SessionCreateRequest {
    private String email;
    private String name;
}