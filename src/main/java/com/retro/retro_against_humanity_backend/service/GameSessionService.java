package com.retro.retro_against_humanity_backend.service;

import com.retro.retro_against_humanity_backend.dto.GameStateDto;
import com.retro.retro_against_humanity_backend.dto.LeaveSessionResult;
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
        ActiveSession session = getSessionByCode(sessionCode);

        sessionPlayerRepository.findByUserAndSession(user, session).orElseGet(() -> {

            Integer maxTurnOrder = sessionPlayerRepository.findMaxTurnOrderBySession(session)
                    .orElse(0);

            SessionPlayer newPlayer = new SessionPlayer();
            newPlayer.setSession(session);
            newPlayer.setUser(user);
            newPlayer.setScore(0);
            newPlayer.setTurnOrder(maxTurnOrder + 1);
            return sessionPlayerRepository.save(newPlayer);
        });

        boolean sessionUpdated = false;
        if (session.getHostUserId() == null) {
            session.setHostUserId(user.getId());
            sessionUpdated = true;
        }

        if (session.getCardHolderId() == null) {
            session.setCardHolderId(user.getId());
            sessionUpdated = true;
        }
        if(sessionUpdated) {
            sessionRepository.save(session);
        }
    }

    @Transactional
    public LeaveSessionResult leaveSession(String sessionCode, String username) {
        Users leavingUser = getUserByUsername(username);
        ActiveSession session = getSessionByCode(sessionCode);
        Long leavingUserId = leavingUser.getId();

        Long oldHostId = session.getHostUserId();
        Long oldCardHolderId = session.getCardHolderId();

        List<SessionPlayer> playersBeforeRemoval = sessionPlayerRepository.findBySessionOrderByCreatedAtAsc(session);

        sessionPlayerRepository.deleteByUserAndSession(leavingUser, session);

        if (playersBeforeRemoval.size() == 1) {
            sessionRepository.delete(session);
            return LeaveSessionResult.sessionDeleted();
        }

        List<SessionPlayer> remainingPlayers = playersBeforeRemoval.stream()
                .filter(p -> !p.getUser().getId().equals(leavingUserId))
                .toList();

        if (leavingUserId.equals(oldHostId)) {
            SessionPlayer newHost = remainingPlayers.get(0);
            session.setHostUserId(newHost.getUser().getId());
        }

        if (leavingUserId.equals(oldCardHolderId)) {
            int leavingPlayerIndex = findPlayerIndexByUserId(playersBeforeRemoval, leavingUserId);

            int newCardHolderIndex = (leavingPlayerIndex + 1) % playersBeforeRemoval.size();

            SessionPlayer nextPlayerInOriginalOrder = playersBeforeRemoval.get(newCardHolderIndex);

            if(nextPlayerInOriginalOrder.getUser().getId().equals(leavingUserId)) {
                newCardHolderIndex = (newCardHolderIndex + 1) % playersBeforeRemoval.size();
                nextPlayerInOriginalOrder = playersBeforeRemoval.get(newCardHolderIndex);
            }

            session.setCardHolderId(nextPlayerInOriginalOrder.getUser().getId());
        }

        sessionRepository.save(session);

        return new LeaveSessionResult(
                false,
                oldHostId,
                session.getHostUserId(),
                oldCardHolderId,
                session.getCardHolderId()
        );
    }

    @Transactional
    public boolean endRound(String sessionCode) {
        ActiveSession session = getSessionByCode(sessionCode);

        List<SessionPlayer> players = sessionPlayerRepository.findBySessionOrderByTurnOrderAsc(session);
        if (players.isEmpty()) {
            sessionRepository.delete(session);
            return true;
        }

        Long currentCardHolderId = session.getCardHolderId();
        int currentIndex = findPlayerIndexByUserId(players, currentCardHolderId);

        int nextIndex = (currentIndex + 1) % players.size();
        SessionPlayer nextCardHolder = players.get(nextIndex);

        if (players.size() == 1) { //TODO: Unsure if needed, this case
            sessionRepository.delete(session);
            return true;
        }

        session.setCardHolderId(nextCardHolder.getUser().getId());
        sessionRepository.save(session);
        return false;
    }

    private int findPlayerIndexByUserId(List<SessionPlayer> players, Long userId) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getUser().getId().equals(userId)) {
                return i;
            }
        }
        return -1;
    }

    private Users getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
    }

    private ActiveSession getSessionByCode(String sessionCode) {
        return sessionRepository.findByCode(sessionCode)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionCode));
    }

    @Transactional
    public GameStateDto getGameState(String sessionCode) {
        ActiveSession session = sessionRepository.findByCode(sessionCode)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionCode));

        List<PlayerDto> players = sessionPlayerRepository.findBySession(session).stream()
                .map(sp -> new PlayerDto(sp.getUser().getId(), sp.getUser().getUsername()))
                .collect(Collectors.toList());

        boolean gameStarted = sessionRepository.findSessionStartedByCode(sessionCode)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionCode));

        return new GameStateDto(session.getCode(), session.getHostUserId(), session.getCardHolderId(), players, gameStarted);
    }

    @Transactional
    public void endSession(String sessionCode) {
//        ActiveSession session = sessionRepository.findByCode(sessionCode)
//                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionCode));
//        sessionRepository.delete(session);
        //TODO: Probably not needed, as sessions are deleted when the last player leaves
        //TODO: implement logic to save points, etc. before deleting the session
    }

    @Transactional
    public void startSession(String sessionCode) {
        ActiveSession session = sessionRepository.findByCode(sessionCode)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionCode));
        session.setSessionStarted(true);
        sessionRepository.save(session);
    }
}