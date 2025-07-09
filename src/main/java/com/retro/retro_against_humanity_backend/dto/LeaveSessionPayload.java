package com.retro.retro_against_humanity_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveSessionPayload {
    private String sessionCode;
    private String username;
}