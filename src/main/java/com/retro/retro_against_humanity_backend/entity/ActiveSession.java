package com.retro.retro_against_humanity_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "active_session")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "host_user_id")
    private Long hostUserId;

    @Column(name = "card_holder_id")
    private Long cardHolderId;

    @Column(name = "session_started", nullable = false)
    private boolean sessionStarted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}