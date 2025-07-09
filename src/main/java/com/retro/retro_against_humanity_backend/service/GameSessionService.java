package com.retro.retro_against_humanity_backend.service;

import com.retro.retro_against_humanity_backend.dto.GameStateDto;
import com.retro.retro_against_humanity_backend.dto.PlayerDto;
import com.retro.retro_against_humanity_backend.entity.ActiveSession;
import com.retro.retro_against_humanity_backend.entity.SessionPlayer;
import com.retro.retro_against_humanity_backend.entity.Users;
import com.retro.retro_against_humanity_backend.repository.ActiveSessionRepository;
import com.retro.retro_against_humanity_backend.repository.SessionPlayerRepository;
import com.retro.retro_against_humanity_backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameSessionService {

    private final ActiveSessionRepository sessionRepository;
    private final SessionPlayerRepository sessionPlayerRepository;
    private final UserRepository userRepository;

    @Transactional
    public void joinSession(String sessionCode, String username, String email) {

        Users user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.save(new Users(null, email, username, 0)));

        ActiveSession session = sessionRepository.findByCode(sessionCode)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionCode));

        sessionPlayerRepository.findByUserAndSession(user, session).orElseGet(() -> {
            SessionPlayer newPlayer = new SessionPlayer();
            newPlayer.setSession(session);
            newPlayer.setUser(user);
            newPlayer.setScore(0);
            return sessionPlayerRepository.save(newPlayer);
        });

        if (session.getHostUserId() == null) {
            session.setHostUserId(user.getId());
            sessionRepository.save(session);
        }
    }

    @Transactional
    public void leaveSession(String sessionCode, String username) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        ActiveSession session = sessionRepository.findByCode(sessionCode)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionCode));

        sessionPlayerRepository.deleteByUserAndSession(user, session);

        if (user.getId().equals(session.getHostUserId())) {
            Optional<SessionPlayer> nextHostPlayer = sessionPlayerRepository
                    .findFirstBySessionOrderByCreatedAtAsc(session);

            if (nextHostPlayer.isPresent()) {
                session.setHostUserId(nextHostPlayer.get().getUser().getId());
            } else {
                session.setHostUserId(null);
            }
            sessionRepository.save(session);
        }
    }

    @Transactional
    public GameStateDto getGameState(String sessionCode) {
        ActiveSession session = sessionRepository.findByCode(sessionCode)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionCode));

        List<PlayerDto> players = sessionPlayerRepository.findBySession(session).stream()
                .map(sp -> new PlayerDto(sp.getUser().getId(), sp.getUser().getUsername()))
                .collect(Collectors.toList());

        return new GameStateDto(session.getCode(), session.getHostUserId(), players);
    }
}