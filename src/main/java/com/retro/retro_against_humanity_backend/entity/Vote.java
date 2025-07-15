package com.retro.retro_against_humanity_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "votes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comment_id", nullable = false)
    private Long commentId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "voter_player_id")
    private SessionPlayer voter;

    public Vote(Long commentId, SessionPlayer voter) {
        this.commentId = commentId;
        this.voter = voter;
    }
}