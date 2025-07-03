package com.retro.retro_against_humanity_backend.Service;

import com.retro.retro_against_humanity_backend.Constants.Const;
import com.retro.retro_against_humanity_backend.Entity.ActiveSession;
import com.retro.retro_against_humanity_backend.Repository.SessionRepository;
import com.retro.retro_against_humanity_backend.Dto.SessionCreateRequest;
import com.retro.retro_against_humanity_backend.error.SessionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;

    public String create(SessionCreateRequest request) {
        String sessionId = UUID.randomUUID().toString().substring(0, 6);

        ActiveSession session = new ActiveSession();
        session.setCode(sessionId);
        session.setEmail(request.getEmail());
        session.setUsername(request.getName());

        sessionRepository.save(session);

        return sessionId;
    }

    public void checkActiveSessions(String sessionCode) {
        boolean session = sessionRepository.existsByCode(sessionCode);
        if (!session) {
            throw new SessionNotFoundException(Const.SESSION_NOT_FOUND_MESSAGE);
        }
    }

    public void deleteSession(String sessionCode) {
        ActiveSession session = sessionRepository.findById(sessionCode)
                .orElseThrow(() -> new SessionNotFoundException(Const.SESSION_NOT_FOUND_MESSAGE));

        sessionRepository.delete(session);
    }
}
