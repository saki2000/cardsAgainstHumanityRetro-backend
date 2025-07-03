package com.retro.retro_against_humanity_backend.Dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class SessionCreateRequest {
    @NotNull(message = "Email cannot be null")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Name cannot be null")
    private String name;
}