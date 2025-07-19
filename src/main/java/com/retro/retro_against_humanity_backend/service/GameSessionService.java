package com.retro.retro_against_humanity_backend.service;

import com.retro.retro_against_humanity_backend.dto.*;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameSessionService {

    private final ActiveSessionRepository sessionRepository;
    private final SessionPlayerRepository sessionPlayerRepository;
    private final UserRepository userRepository;
    private final CardService cardService;

    @Transactional
    public Users joinSession(String sessionCode, String username, String email) {

        Users user = findOrCreateUser(username, email);
        ActiveSession session = getSessionByCode(sessionCode);
        findSessionPlayer(user, session);
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
        List<PlayerDto> players = getPlayersFromSession(session);
        boolean gameStarted = getSessionStarted(sessionCode);
        Map<String, CardDto> slots = cardService.getPlayedCardsForRound(sessionCode);

        return new GameStateDto(session.getCode(), session.getHostUserId(), session.getCardHolderId(), players, gameStarted,
                slots);
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

    private void findSessionPlayer(Users user, ActiveSession session) {
        sessionPlayerRepository.findByUserAndSession(user, session).orElseGet(() -> {
            Integer maxTurnOrder = sessionPlayerRepository.findMaxTurnOrderBySession(session)
                    .orElse(0); // If no players, maxTurnOrder is 0.

            SessionPlayer newPlayer = new SessionPlayer();
            newPlayer.setSession(session);
            newPlayer.setUser(user);
            newPlayer.setScore(0);
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
}