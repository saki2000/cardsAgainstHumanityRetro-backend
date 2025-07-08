package com.retro.retro_against_humanity_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPayload {
    private String username;
    private String email;
    private String sessionCode;
}
