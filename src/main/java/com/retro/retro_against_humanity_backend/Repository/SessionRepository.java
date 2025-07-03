package com.retro.retro_against_humanity_backend.Repository;

import com.retro.retro_against_humanity_backend.Entity.ActiveSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<ActiveSession, String> {
    boolean existsByCode(String code);
    void deleteByCode(String code);
}
