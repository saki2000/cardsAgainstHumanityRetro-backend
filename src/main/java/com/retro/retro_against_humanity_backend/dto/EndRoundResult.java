package com.retro.retro_against_humanity_backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class EndRoundResult {
    private final boolean sessionEnded;
    private final Long newCardHolderId;
    private final List<CardDto> cardsToDeal;

    public static EndRoundResult sessionEnded() {
        return new EndRoundResult(true, null, null);
    }
}
