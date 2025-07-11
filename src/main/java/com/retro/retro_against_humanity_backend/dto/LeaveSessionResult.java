package com.retro.retro_against_humanity_backend.dto;

public record LeaveSessionResult(
        boolean wasSessionDeleted,
        Long oldHostId,
        Long newHostId,
        Long oldCardHolderId,
        Long newCardHolderId
) {
    public static LeaveSessionResult sessionDeleted() {
        return new LeaveSessionResult(true, null, null, null, null);
    }
}