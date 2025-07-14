package com.retro.retro_against_humanity_backend.payloads;

public record PlayCardPayload (String sessionCode, Long cardId, String slotId  ) {}