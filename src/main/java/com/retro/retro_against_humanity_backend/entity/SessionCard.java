package com.retro.retro_against_humanity_backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "session_card")
@Data
public class SessionCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "session_id", referencedColumnName = "id")
    private ActiveSession session;

    @ManyToOne(optional = false)
    @JoinColumn(name = "card_id")
    private Card card;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public enum Status {
        FREE, PLAYED
    }

}
