package com.retro.retro_against_humanity_backend.repository;

import com.retro.retro_against_humanity_backend.entity.ActiveSession;
import com.retro.retro_against_humanity_backend.entity.SessionHistory;
import com.retro.retro_against_humanity_backend.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionHistoryRepository extends JpaRepository<SessionHistory, Long> {
    Optional<SessionHistory> findByUserAndSession(Users user, ActiveSession session);
    List<SessionHistory> findBySession(ActiveSession session);

    @Query("SELECT u.email FROM SessionHistory sh JOIN sh.user u WHERE sh.session.id = :sessionId")
    List<String> findUserEmailsBySessionId(Long sessionId);
}
