package com.retro.retro_against_humanity_backend.repository;

import com.retro.retro_against_humanity_backend.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    @Query("""
    SELECT c FROM Card c
    WHERE c.id NOT IN (
        SELECT sc.card.id FROM SessionCard sc
        WHERE sc.session.code = :sessionCode
    )
""")
    List<Card> findAvailableCards(@Param("sessionCode") String sessionCode);

    @Query("""
    SELECT c FROM Card c
    WHERE c.type = :type
    AND c.id NOT IN 
        (SELECT sc.card.id 
         FROM SessionCard sc 
         WHERE sc.session.code = :sessionCode)
 """)
    List<Card> findAvailableCardsByType(@Param("sessionCode") String sessionCode, @Param("type") Card.CardType type);
}
