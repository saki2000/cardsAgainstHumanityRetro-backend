package com.retro.retro_against_humanity_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Formula;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "session_card_id")
    private SessionCard sessionCard;

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_player_id")
    private SessionPlayer authorPlayer;

    @Column(name = "content", nullable = false)
    private String content;

    @Formula("(select count(*) from votes v where v.comment_id = id)")
    private int voteCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
