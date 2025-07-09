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

        // Check if player is already in the session
        if (sessionPlayerRepository.findByUserAndSession(user, session).isEmpty()) {
            // Find the highest current turn_order in the session to assign the next one
            Integer maxTurnOrder = sessionPlayerRepository.findMaxTurnOrderBySession(session)
                    .orElse(0);

            SessionPlayer newPlayer = new SessionPlayer();
            newPlayer.setSession(session);
            newPlayer.setUser(user);
            newPlayer.setScore(0);
            newPlayer.setTurnOrder(maxTurnOrder + 1); // Assign the next turn order
            sessionPlayerRepository.save(newPlayer);
        }

        if (session.getHostUserId() == null) {
            session.setHostUserId(user.getId());
            sessionRepository.save(session);
        }

        if (session.getCurrentPlayerId() == null) {
            session.setCurrentPlayerId(user.getId());
            sessionRepository.save(session);
        }
    }

    @Transactional
    public void leaveSession(String sessionCode, String username, Runnable broadcastLeave, Runnable broadcastHostChange) {
        Users user = getUserByUsername(username);
        ActiveSession session = getSessionByCode(sessionCode);

        // Important: Get player info *before* deleting them
        Optional<SessionPlayer> leavingPlayerOpt = sessionPlayerRepository.findByUserAndSession(user, session);

        if (leavingPlayerOpt.isPresent()) {
            removePlayerFromSession(user, session);
            broadcastLeave.run();

            handleCurrentPlayerReassignment(user, session, leavingPlayerOpt.get());
            handleHostReassignmentOrSessionDeletion(user, session, broadcastHostChange);
        }
    }

    private Users getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
    }

    private ActiveSession getSessionByCode(String sessionCode) {
        return sessionRepository.findByCode(sessionCode)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionCode));
    }

    private void removePlayerFromSession(Users user, ActiveSession session) {
        sessionPlayerRepository.deleteByUserAndSession(user, session);
    }

    private void handleCurrentPlayerReassignment(Users user, ActiveSession session, SessionPlayer leavingPlayer) {
        if (user.getId().equals(session.getCurrentPlayerId())) {
            int currentTurnOrder = leavingPlayer.getTurnOrder();

            // Find the next player in the sequence
            Optional<SessionPlayer> nextPlayerOpt = sessionPlayerRepository
                    .findFirstBySessionAndTurnOrderGreaterThanOrderByTurnOrderAsc(session, currentTurnOrder);

            if (nextPlayerOpt.isPresent()) {
                // The next player is found
                session.setCurrentPlayerId(nextPlayerOpt.get().getUser().getId());
            } else {
                // Wrap around: find the player with the lowest turn order
                sessionPlayerRepository.findFirstBySessionOrderByTurnOrderAsc(session)
                        .ifPresentOrElse(
                                firstPlayer -> session.setCurrentPlayerId(firstPlayer.getUser().getId()),
                                () -> session.setCurrentPlayerId(null) // No players left
                        );
            }
            sessionRepository.save(session);
        }
    }

    private void handleHostReassignmentOrSessionDeletion(Users user, ActiveSession session, Runnable broadcastHostChange) {
        if (user.getId().equals(session.getHostUserId())) {
            // Assign host to the player with the lowest turn order
            Optional<SessionPlayer> nextHostPlayer = sessionPlayerRepository
                    .findFirstBySessionOrderByTurnOrderAsc(session);

            if (nextHostPlayer.isPresent()) {
                session.setHostUserId(nextHostPlayer.get().getUser().getId());
                sessionRepository.save(session);
                broadcastHostChange.run();
            } else {
                // No players left, delete the session
                sessionRepository.delete(session);
            }
        }
    }

    @Transactional
    public GameStateDto getGameState(String sessionCode) {
        ActiveSession session = sessionRepository.findByCode(sessionCode)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionCode));

        List<PlayerDto> players = sessionPlayerRepository.findBySession(session).stream()
                .map(sp -> new PlayerDto(sp.getUser().getId(), sp.getUser().getUsername()))
                .collect(Collectors.toList());

        return new GameStateDto(session.getCode(), session.getHostUserId(), session.getCurrentPlayerId(), players);
    }
}