package com.retro.retro_against_humanity_backend.socket;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.retro.retro_against_humanity_backend.dto.GameStateDto;
import com.retro.retro_against_humanity_backend.dto.JoinSessionPayload;
import com.retro.retro_against_humanity_backend.dto.LeaveSessionResult;
import com.retro.retro_against_humanity_backend.service.GameSessionService;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
@RequiredArgsConstructor
public class GameSocketModule {
    private final SocketIOServer server;
    private final GameSessionService gameSessionService;

    private final Map<String, ClientData> clientDataMap = new ConcurrentHashMap<>();
    private record ClientData(String username, String sessionCode) {}


    @PostConstruct
    public void init() {
        server.addConnectListener(this::onConnect);
        server.addDisconnectListener(this::onDisconnect);
        server.addEventListener("join_session", JoinSessionPayload.class, this::onJoinSession);
//        server.addEventListener("leave_session", LeaveSessionPayload.class, this::onLeaveSession);
    }

    private void onConnect(SocketIOClient client) {
        System.out.println("Client connected: " + client.getSessionId());
    }

    private void onDisconnect(SocketIOClient client) {
        System.out.println("Client disconnected: " + client.getSessionId());
        ClientData data = clientDataMap.remove(client.getSessionId().toString());

        if (data != null) {
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
                }
                broadcastGameState(data.sessionCode());
            }
        }
    }

    private void onJoinSession(SocketIOClient client, JoinSessionPayload payload, AckRequest ackRequest) {
        clientDataMap.put(client.getSessionId().toString(), new ClientData(payload.getUsername(), payload.getSessionCode()));
        client.joinRoom(payload.getSessionCode());
        gameSessionService.joinSession(payload.getSessionCode(), payload.getUsername(), payload.getEmail());
        broadcastGameState(payload.getSessionCode());
        server.getRoomOperations(payload.getSessionCode()).sendEvent("player_joined", payload.getUsername());
    }

//    //TODO: Think this is just disconnect now, not leave - same same(?)
//    private void onLeaveSession(SocketIOClient client, LeaveSessionPayload payload, AckRequest ackRequest) {
//        client.leaveRoom(payload.getSessionCode());
//        gameSessionService.leaveSession(payload.getSessionCode(), payload.getUsername());
//        broadcastGameState(payload.getSessionCode());
//        server.getRoomOperations(payload.getSessionCode()).sendEvent("player_left", payload.getUsername());
//    }

    private void broadcastGameState(String sessionCode) {
        try {
            GameStateDto gameState = gameSessionService.getGameState(sessionCode);
            server.getRoomOperations(sessionCode).sendEvent("game_state_update", gameState);
        } catch (EntityNotFoundException e) {
            System.err.println("Attempted to broadcast state for a non-existent or empty session: " + sessionCode);
        }
    }
}
