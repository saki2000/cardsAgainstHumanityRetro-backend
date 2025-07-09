package com.retro.retro_against_humanity_backend.repository;

import com.retro.retro_against_humanity_backend.entity.ActiveSession;
import com.retro.retro_against_humanity_backend.entity.SessionPlayer;
import com.retro.retro_against_humanity_backend.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
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
}