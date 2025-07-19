package com.retro.retro_against_humanity_backend.repository;

import com.retro.retro_against_humanity_backend.entity.SessionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionHistoryRepository extends JpaRepository<SessionHistory, Long> {
}
