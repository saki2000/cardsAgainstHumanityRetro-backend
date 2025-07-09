package com.retro.retro_against_humanity_backend.repository;

import com.retro.retro_against_humanity_backend.entity.ActiveSession;
import com.retro.retro_against_humanity_backend.entity.SessionPlayer;
import com.retro.retro_against_humanity_backend.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionPlayerRepository extends JpaRepository<SessionPlayer, Long> {
    boolean existsByUserAndSession(Users user, ActiveSession session);
    List<SessionPlayer> findBySession(ActiveSession session);
    void deleteByUserAndSession(Users user, ActiveSession session);

    Optional<SessionPlayer> findByUserAndSession(Users user, ActiveSession session);
    Optional<SessionPlayer> findFirstBySessionOrderByCreatedAtAsc(ActiveSession session);

    @Query("SELECT MAX(sp.turnOrder) FROM SessionPlayer sp WHERE sp.session = :session")
    Optional<Integer> findMaxTurnOrderBySession(@Param("session") ActiveSession session);
    Optional<SessionPlayer> findFirstBySessionAndTurnOrderGreaterThanOrderByTurnOrderAsc(ActiveSession session, Integer turnOrder);
    Optional<SessionPlayer> findFirstBySessionOrderByTurnOrderAsc(ActiveSession session);
}