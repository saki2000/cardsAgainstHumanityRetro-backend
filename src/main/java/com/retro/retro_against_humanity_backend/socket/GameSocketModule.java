package com.retro.retro_against_humanity_backend.socket;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.retro.retro_against_humanity_backend.dto.UserPayload;
import com.retro.retro_against_humanity_backend.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GameSocketModule {
    private final SocketIOServer server;
    private final UserService userService;

    @PostConstruct
    public void init() {
        server.addConnectListener(this::onConnect);
        server.addDisconnectListener(this::onDisconnect);
        server.addEventListener("join_session", UserPayload.class, this::onJoinSession);
    }

    private void onConnect(SocketIOClient client) {
        System.out.println("Client connected: " + client.getSessionId());
    }

    private void onDisconnect(SocketIOClient client) {
        String sessionCode = client.get("sessionCode");
        String username = client.get("username");
        if (sessionCode != null && username != null) {
            userService.removeUserFromSessionByUsername(username, sessionCode);
            List<UserPayload> allUsers = userService.getAllUsersInSession(sessionCode);
            server.getRoomOperations(sessionCode).sendEvent("update_user_list", allUsers);
        }
        System.out.println("Client disconnected: " + client.getSessionId());
    }

    private void onJoinSession(SocketIOClient client, UserPayload payload, AckRequest ackRequest) {
        String sessionCode = payload.getSessionCode();
        if (sessionCode == null || sessionCode.trim().isEmpty()) {
            client.sendEvent("error", "Session code is required.");
            return;
        }
        client.set("sessionCode", sessionCode);
        client.set("username", payload.getUsername());
        client.joinRoom(sessionCode);
        List<UserPayload> allUsers = userService.addUserToSessionAndGetAll(payload, sessionCode);
        server.getRoomOperations(sessionCode).sendEvent("update_user_list", allUsers);
    }
}
