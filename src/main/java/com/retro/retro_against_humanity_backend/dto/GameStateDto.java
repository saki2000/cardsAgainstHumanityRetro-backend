package com.retro.retro_against_humanity_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameStateDto {
    private String sessionCode;
    private int roundNumber;
    private Long hostId;
    private Long cardHolderId;
    private List<PlayerDto> players;
    private boolean sessionStarted;
    Map<String, CardDto> slots;
}