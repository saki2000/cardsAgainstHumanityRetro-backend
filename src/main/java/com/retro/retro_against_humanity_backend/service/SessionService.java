package com.retro.retro_against_humanity_backend.service;

import com.retro.retro_against_humanity_backend.dto.SessionCreateRequest;
import com.retro.retro_against_humanity_backend.errors.Constants;
import com.retro.retro_against_humanity_backend.entity.ActiveSession;
import com.retro.retro_against_humanity_backend.repository.ActiveSessionRepository;
import com.retro.retro_against_humanity_backend.exceptions.SessionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final ActiveSessionRepository activeSessionRepository;

    public String create(SessionCreateRequest request) {
        String sessionId = UUID.randomUUID().toString().substring(0, 6);

        ActiveSession session = new ActiveSession();
        session.setCode(sessionId);

        activeSessionRepository.save(session);

        return sessionId;
    }

    public void checkActiveSessions(String sessionCode) {
        boolean session = activeSessionRepository.existsByCode(sessionCode);
        if (!session) {
            throw new SessionNotFoundException(Constants.Session.SESSION_NOT_FOUND_MESSAGE);
        }
    }

    @Transactional
    public void deleteSession(String sessionCode) {
        if (!activeSessionRepository.existsByCode(sessionCode)) {
            throw new SessionNotFoundException(Constants.Session.SESSION_NOT_FOUND_MESSAGE);
        }
        activeSessionRepository.deleteByCode(sessionCode);
    }
}
