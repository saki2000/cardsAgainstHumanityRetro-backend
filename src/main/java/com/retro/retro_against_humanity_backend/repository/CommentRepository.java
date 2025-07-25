package com.retro.retro_against_humanity_backend.repository;

import com.retro.retro_against_humanity_backend.dto.CardCommentDTO;
import com.retro.retro_against_humanity_backend.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT new com.retro.retro_against_humanity_backend.dto.CardCommentDTO(card.content, comment.content) " +
            "FROM Comment comment " +
            "JOIN comment.sessionCard sc " +
            "JOIN sc.card card " +
            "WHERE (:sessionId IS NULL OR sc.session.id = :sessionId)")
    List<CardCommentDTO> findCardContentAndCommentsBySessionId(Long sessionId);
}
