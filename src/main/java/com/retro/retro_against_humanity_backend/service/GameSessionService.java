package com.retro.retro_against_humanity_backend.service;

import com.retro.retro_against_humanity_backend.dto.*;
import com.retro.retro_against_humanity_backend.entity.ActiveSession;
import com.retro.retro_against_humanity_backend.entity.SessionHistory;
import com.retro.retro_against_humanity_backend.entity.SessionPlayer;
import com.retro.retro_against_humanity_backend.entity.Users;
import com.retro.retro_against_humanity_backend.repository.ActiveSessionRepository;
import com.retro.retro_against_humanity_backend.repository.SessionHistoryRepository;
import com.retro.retro_against_humanity_backend.repository.SessionPlayerRepository;
import com.retro.retro_against_humanity_backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameSessionService {

    private final ActiveSessionRepository sessionRepository;
    private final SessionPlayerRepository sessionPlayerRepository;
    private final UserRepository userRepository;
    private final CardService cardService;
    private final SessionHistoryRepository sessionHistoryRepository;
    private final UserService userService;

    @Transactional
    public Users joinSession(String sessionCode, String username, String email) {

        Users user = findOrCreateUser(username, email);
        ActiveSession session = getSessionByCode(sessionCode);
        int previousScoreInThisSession = addToSessionHistory(session, user);
        findSessionPlayer(user, session, previousScoreInThisSession);
        updateSessionHostAndCardHolder(session, user);

        return user;
    }

    @Transactional
    public LeaveSessionResult leaveSession(String sessionCode, String username) {
        Users leavingUser = getUserByUsername(username);
        ActiveSession session = getSessionByCode(sessionCode);
        Long leavingUserId = leavingUser.getId();

        Long oldHostId = session.getHostUserId();
        Long oldCardHolderId = session.getCardHolderId();

        List<SessionPlayer> players = sessionPlayerRepository.findBySessionOrderByCreatedAtAsc(session);

        sessionPlayerRepository.deleteByUserAndSession(leavingUser, session);

        if (players.size() == 1) {
            sessionRepository.delete(session);
            return LeaveSessionResult.sessionDeleted();
        }

        updateHostIfNeeded(session, leavingUserId, players);
        updateCardHolderIfNeeded(session, leavingUserId, players);

        sessionRepository.save(session);

        return new LeaveSessionResult(false, oldHostId, session.getHostUserId(), oldCardHolderId, session.getCardHolderId());
    }

    @Transactional
    public EndRoundResult endRound(String sessionCode, int numberOfCards) {
        cardService.clearPlayedCardSlots(sessionCode);
        ActiveSession session = getSessionByCode(sessionCode);

        List<SessionPlayer> players = sessionPlayerRepository.findBySessionOrderByTurnOrderAsc(session);
        if (players.isEmpty()) {
            return EndRoundResult.sessionEnded();
        }

        int currentIndex = -1;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getUser().getId().equals(session.getCardHolderId())) {
                currentIndex = i;
                break;
            }
        }
        int nextIndex = (currentIndex + 1) % players.size();
        Long newCardHolderId = players.get(nextIndex).getUser().getId();
        session.setCardHolderId(newCardHolderId);
        sessionRepository.save(session);

        List<CardDto> cardsToDealDto = cardService.getCardsForNextRound(sessionCode, numberOfCards);

        return new EndRoundResult(false, newCardHolderId, cardsToDealDto);
    }

    @Transactional
    public GameStateDto getGameState(String sessionCode) {
        ActiveSession session = getSessionByCode(sessionCode);
        int roundNumber = session.getRoundNumber();
        List<PlayerDto> players = getPlayersFromSession(session);
        boolean gameStarted = getSessionStarted(sessionCode);
        Map<String, CardDto> slots = cardService.getPlayedCardsForRound(sessionCode);

        return new GameStateDto(session.getCode(), roundNumber, session.getHostUserId(), session.getCardHolderId(), players, gameStarted,
                slots);
    }

    @Transactional
    public void endSession(String sessionCode) {
        ActiveSession session = getSessionByCode(sessionCode);

        updateHistoryScores(session);
        userService.updateStatsOnGameEnd(session);
    }

    @Transactional
    public void startSession(String sessionCode) {
        ActiveSession session = getSessionByCode(sessionCode);
        session.setSessionStarted(true);
        updateRound(sessionCode);
        sessionRepository.save(session);
    }

    @Transactional
    public void updateRound(String sessionCode) {
        ActiveSession session = getSessionByCode(sessionCode);
        session.setRoundNumber(session.getRoundNumber() + 1);
        updateHistoryScores(session);
        sessionRepository.save(session);
    }

    private Users getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
    }

    private ActiveSession getSessionByCode(String sessionCode) {
        return sessionRepository.findByCode(sessionCode)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionCode));
    }

    private boolean getSessionStarted(String sessionCode) {
        return sessionRepository.findSessionStartedByCode(sessionCode)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionCode));
    }

    private Users findOrCreateUser(String username, String email) {
        return userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.save(new Users(null, email, username, 0, 0, 0)));
    }

    private int addToSessionHistory(ActiveSession session, Users user) {
        //TODO: check? to prevent some race conditions ?
        ActiveSession managedSession = getSessionByCode(session.getCode());

        return sessionHistoryRepository.findByUserAndSession(user, session)
                .map(SessionHistory::getScore)
                .orElseGet(() -> {
                    SessionHistory newHistory = new SessionHistory();
                    newHistory.setUser(user);
                    newHistory.setSession(managedSession);
                    newHistory.setScore(0);
                    sessionHistoryRepository.save(newHistory);
                    return 0;
                });
    }

    private void findSessionPlayer(Users user, ActiveSession session, int previousScoreInThisSession) {
        sessionPlayerRepository.findByUserAndSession(user, session).orElseGet(() -> {
            Integer maxTurnOrder = sessionPlayerRepository.findMaxTurnOrderBySession(session)
                    .orElse(0); // If no players, maxTurnOrder is 0.

            SessionPlayer newPlayer = new SessionPlayer();
            newPlayer.setSession(session);
            newPlayer.setUser(user);
            newPlayer.setScore(previousScoreInThisSession);
            newPlayer.setTurnOrder(maxTurnOrder + 1); // New player gets the next turn order.
            return sessionPlayerRepository.save(newPlayer);
        });
    }

    private void  updateSessionHostAndCardHolder(ActiveSession session, Users user) {
        boolean sessionUpdated = false;
        if (session.getHostUserId() == null) {
            session.setHostUserId(user.getId());
            sessionUpdated = true;
        }
        if (session.getCardHolderId() == null) {
            session.setCardHolderId(user.getId());
            sessionUpdated = true;
        }
        if (sessionUpdated) {
            sessionRepository.save(session);
        }
    }

    private List<PlayerDto> getPlayersFromSession(ActiveSession session) {
        return sessionPlayerRepository.findBySession(session).stream()
                .map(sp -> new PlayerDto(
                        sp.getUser().getId(),
                        sp.getUser().getUsername(),
                        sp.getScore()
                ))
                .collect(Collectors.toList());
    }

    private int findPlayerIndexByUserId(List<SessionPlayer> players, Long userId) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getUser().getId().equals(userId)) {
                return i;
            }
        }
        return -1;
    }

    private void updateHostIfNeeded(ActiveSession session, Long leavingUserId, List<SessionPlayer> players) {
        if (leavingUserId.equals(session.getHostUserId())) {
            SessionPlayer newHost = players.stream()
                    .filter(p -> !p.getUser().getId().equals(leavingUserId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No players left to assign as host."));
            session.setHostUserId(newHost.getUser().getId());
        }
    }

    private void updateCardHolderIfNeeded(ActiveSession session, Long leavingUserId, List<SessionPlayer> players) {
        if (leavingUserId.equals(session.getCardHolderId())) {
            int leavingPlayerIndex = findPlayerIndexByUserId(players, leavingUserId);
            int nextPlayerIndex = (leavingPlayerIndex + 1) % players.size();
            SessionPlayer newCardHolder = players.get(nextPlayerIndex);

            // If the next player is the one who is leaving, skip to the following one.
            if (newCardHolder.getUser().getId().equals(leavingUserId)) {
                nextPlayerIndex = (nextPlayerIndex + 1) % players.size();
                newCardHolder = players.get(nextPlayerIndex);
            }
            session.setCardHolderId(newCardHolder.getUser().getId());
        }
    }

    private void updateHistoryScores(ActiveSession session){
        //TODO: check? to prevent some race conditions ?
        ActiveSession managedSession = getSessionByCode(session.getCode());

        List<SessionPlayer> players = sessionPlayerRepository.findBySession(managedSession);
        for (SessionPlayer player : players) {
            sessionHistoryRepository.findByUserAndSession(player.getUser(), managedSession)
                    .ifPresent(history -> {
                        history.setScore(player.getScore());
                        sessionHistoryRepository.save(history);
                    });
        }
    }
}