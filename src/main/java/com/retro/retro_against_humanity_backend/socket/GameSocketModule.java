package com.retro.retro_against_humanity_backend.socket;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.retro.retro_against_humanity_backend.dto.*;
import com.retro.retro_against_humanity_backend.entity.Users;
import com.retro.retro_against_humanity_backend.payloads.*;
import com.retro.retro_against_humanity_backend.service.CardService;
import com.retro.retro_against_humanity_backend.service.GameSessionService;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
@RequiredArgsConstructor
public class GameSocketModule {
    private final int MAX_CARDS_PER_ROUND = 9;
    private final SocketIOServer server;
    private final GameSessionService gameSessionService;
    private final CardService cardService;

    private final Map<String, ClientData> clientDataMap = new ConcurrentHashMap<>();
    private final Map<Long, SocketIOClient> userClientMap = new ConcurrentHashMap<>();
    private record ClientData(Long userId, String username, String sessionCode) {}


    @PostConstruct
    public void init() {
        server.addConnectListener(this::onConnect);
        server.addDisconnectListener(this::onDisconnect);
        server.addEventListener("join_session", JoinSessionPayload.class, this::onJoinSession);
        server.addEventListener("end_of_round", EndRoundPayload.class, this::onEndRound);
        server.addEventListener("end_session", EndSessionPayload.class, this::onEndSession);
        server.addEventListener("start_session", SessionStartedPayload.class, this::onSessionStarted);
        server.addEventListener("play_card", PlayCardPayload.class, this::onPlayCard);
        server.addEventListener("submit_comment", SubmitCommentPayload.class, this::onSubmitComment);
        server.addEventListener("vote_comment", VoteCommentPayload.class,this::onVoteComment);
    }

    private void onConnect(SocketIOClient client) {
        System.out.println("Client connected: " + client.getSessionId());
    }

    private void onDisconnect(SocketIOClient client) {
        System.out.println("Client disconnected: " + client.getSessionId());
        ClientData data = clientDataMap.remove(client.getSessionId().toString());

        if (data != null) {
            userClientMap.remove(data.userId());
            LeaveSessionResult result = gameSessionService.leaveSession(data.sessionCode(), data.username());

            if (result.wasSessionDeleted()) {
                System.out.println("Session " + data.sessionCode() + " ended because the last player left.");
            } else {
                if (!result.oldHostId().equals(result.newHostId())) {
                    System.out.println("Host changed in session " + data.sessionCode());
                    server.getRoomOperations(data.sessionCode()).sendEvent("host_change", result.newHostId());
                }
                if (!result.oldCardHolderId().equals(result.newCardHolderId())) {
                    System.out.println("Card holder changed in session " + data.sessionCode());
                    server.getRoomOperations(data.sessionCode()).sendEvent("cardholder_change", result.newCardHolderId());
                    endRoundAndStartNewOne(data.sessionCode());
                }
                broadcastGameState(data.sessionCode());
            }
        }
    }

    private void onJoinSession(SocketIOClient client, JoinSessionPayload payload, AckRequest ackRequest) {
        Users user = gameSessionService.joinSession(payload.getSessionCode(), payload.getUsername(), payload.getEmail());
        ClientData clientData = new ClientData(user.getId(), user.getUsername(), payload.getSessionCode());
        clientDataMap.put(client.getSessionId().toString(), clientData);
        userClientMap.put(user.getId(), client);
        client.joinRoom(payload.getSessionCode());
        broadcastGameState(payload.getSessionCode());
    }

    private void onEndRound(SocketIOClient client, EndRoundPayload payload, AckRequest ackRequest) {
        endRoundAndStartNewOne(payload.sessionCode());
    }

    private void endRoundAndStartNewOne(String sessionCode) {
        EndRoundResult endRoundResult = gameSessionService.endRound(sessionCode, MAX_CARDS_PER_ROUND);

        if (endRoundResult.isSessionEnded()) {
            server.getRoomOperations(sessionCode).sendEvent("session_ended");
        } else {
            gameSessionService.updateRound(sessionCode);
            broadcastCardsToPlayer(endRoundResult.getNewCardHolderId(), endRoundResult.getCardsToDeal());
            broadcastGameState(sessionCode);
        }
    }

    private void onEndSession (SocketIOClient client, EndSessionPayload endSessionPayload, AckRequest ackRequest) {
        ClientData data = clientDataMap.get(client.getSessionId().toString());
        System.out.println("Ending session: " + data.sessionCode());
        gameSessionService.endSession(data.sessionCode());
        server.getRoomOperations(data.sessionCode()).sendEvent("session_ended");
    }

    private void onSessionStarted(SocketIOClient client, SessionStartedPayload payload, AckRequest ackRequest) {
        ClientData data = clientDataMap.get(client.getSessionId().toString());
        System.out.println("Session started for: " + data.sessionCode());
        gameSessionService.startSession(data.sessionCode());
        broadcastGameState(data.sessionCode());
        List<CardDto> cardHolderCard = cardService.getCardsForNextRound (data.sessionCode(), MAX_CARDS_PER_ROUND);
        broadcastCardsToPlayer(data.userId(), cardHolderCard);
    }

    private void onPlayCard(SocketIOClient client, PlayCardPayload payload, AckRequest ackRequest) {
        ClientData data = clientDataMap.get(client.getSessionId().toString());
        System.out.println("User " + data.username() + " played a card in session " + data.sessionCode());
        cardService.playCard(data.sessionCode(), payload.cardId(), payload.slotId());
        broadcastGameState(data.sessionCode());
    }

    private void onSubmitComment(SocketIOClient client, SubmitCommentPayload payload, AckRequest ackRequest) {
        ClientData data = clientDataMap.get(client.getSessionId().toString());
        cardService.submitComment(payload.sessionCode(), payload.sessionCardId(), payload.content(), data.userId());
        broadcastGameState(data.sessionCode());
    }

    private void onVoteComment(SocketIOClient client, VoteCommentPayload payload, AckRequest ackRequest) {
        ClientData data = clientDataMap.get(client.getSessionId().toString());
        cardService.voteComment(data.sessionCode(), payload.commentId(), data.userId());
        broadcastGameState(data.sessionCode());
    }

    private void broadcastGameState(String sessionCode) {
        try {
            GameStateDto gameState = gameSessionService.getGameState(sessionCode);
            server.getRoomOperations(sessionCode).sendEvent("game_state_update", gameState);
        } catch (EntityNotFoundException e) {
            System.err.println("Attempted to broadcast state for a non-existent or empty session: " + sessionCode);
        }
    }

    private void broadcastCardsToPlayer(Long userId, List<CardDto> cards) {
        SocketIOClient client = userClientMap.get(userId);
        if (client != null) {
            System.out.println("Dealing " + cards.size() + " cards to user " + userId);
            client.sendEvent("deal_cards", cards);
        } else {
            System.err.println("Could not find active client for user ID: " + userId + " to deal cards.");
        }
    }
}
