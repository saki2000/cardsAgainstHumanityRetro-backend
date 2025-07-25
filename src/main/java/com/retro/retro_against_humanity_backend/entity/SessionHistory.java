package com.retro.retro_against_humanity_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "session_history", uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "user_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "session_id", referencedColumnName = "id")
    private ActiveSession session;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private Users user;

    @Column(nullable = false)
    private int score = 0;
}
