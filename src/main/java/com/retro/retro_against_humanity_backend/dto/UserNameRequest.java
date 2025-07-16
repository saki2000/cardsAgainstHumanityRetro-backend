package com.retro.retro_against_humanity_backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserNameRequest {

    @NotEmpty(message = "Username cannot be empty")
    @NotNull(message = "Username cannot be null")
    String userName;
}
