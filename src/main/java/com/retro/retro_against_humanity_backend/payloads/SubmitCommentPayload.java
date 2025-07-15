package com.retro.retro_against_humanity_backend.payloads;

public record SubmitCommentPayload(String sessionCode, long sessionCardId, String commentText) {}