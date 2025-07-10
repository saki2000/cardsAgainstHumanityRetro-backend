package com.retro.retro_against_humanity_backend.payloads;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinSessionPayload {
    private String sessionCode;
    private String username;
    private String email;
}