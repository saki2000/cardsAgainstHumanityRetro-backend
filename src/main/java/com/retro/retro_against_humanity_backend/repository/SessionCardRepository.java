package com.retro.retro_against_humanity_backend.repository;

import com.retro.retro_against_humanity_backend.entity.SessionCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionCardRepository extends JpaRepository<SessionCard, Long> {

    @Query("SELECT sc FROM SessionCard sc WHERE sc.session.code = :sessionCode AND sc.slotId IS NOT NULL")
    List<SessionCard> findPlayedCardsInSession(@Param("sessionCode") String sessionCode);
}