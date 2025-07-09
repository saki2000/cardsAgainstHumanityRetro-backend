package com.retro.retro_against_humanity_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameStateDto {
    private String sessionCode;
    private Long hostId;
    private Long currentPlayerId;
    private List<PlayerDto> players;
}