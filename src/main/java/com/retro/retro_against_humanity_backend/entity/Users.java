package com.retro.retro_against_humanity_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String username;

    @Column(name = "best_score", nullable = false)
    private int bestScore = 0;

    @Column(name = "games_played", nullable = false)
    private int gamesPlayed = 0;

    @Column(name = "total_points", nullable = false)
    private int totalPoints = 0;
}