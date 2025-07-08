package com.retro.retro_against_humanity_backend.repository;

import com.retro.retro_against_humanity_backend.entity.ActiveSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActiveSessionRepository extends JpaRepository<ActiveSession, String> {
    boolean existsByCode(String code);
    void deleteByCode(String code);
    Optional<ActiveSession> findByCode(String code);
}
