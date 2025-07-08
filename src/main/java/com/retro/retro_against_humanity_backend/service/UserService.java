package com.retro.retro_against_humanity_backend.service;

import com.retro.retro_against_humanity_backend.dto.UserPayload;
import com.retro.retro_against_humanity_backend.entity.*;
import com.retro.retro_against_humanity_backend.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final ActiveSessionRepository sessionRepository;
    private final SessionPlayerRepository sessionPlayerRepository;

    @Transactional
    public List<UserPayload> addUserToSessionAndGetAll(UserPayload userPayload, String sessionCode) {
        Users user = userRepository.findByUsername(userPayload.getUsername())
                .orElseGet(() -> userRepository.save(
                        new Users(null, userPayload.getEmail(), userPayload.getUsername(), 0)
                ));

        ActiveSession session = sessionRepository.findByCode(sessionCode)
                .orElseThrow(() -> new EntityNotFoundException("Session with code " + sessionCode + " not found"));

        if (!sessionPlayerRepository.existsByUserAndSession(user, session)) {
            sessionPlayerRepository.save(new SessionPlayer(null, session, user, 0));
        }

        return sessionPlayerRepository.findBySession(session).stream()
                .map(sp -> new UserPayload(sp.getUser().getUsername(), sp.getUser().getEmail(), sessionCode))
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeUserFromSessionByUsername(String username, String sessionCode) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User " + username + " not found"));
        ActiveSession session = sessionRepository.findByCode(sessionCode)
                .orElseThrow(() -> new EntityNotFoundException("Session with code " + sessionCode + " not found"));
        sessionPlayerRepository.deleteByUserAndSession(user, session);
    }

    @Transactional(readOnly = true)
    public List<UserPayload> getAllUsersInSession(String sessionCode) {
        ActiveSession session = sessionRepository.findByCode(sessionCode)
                .orElseThrow(() -> new EntityNotFoundException("Session with code " + sessionCode + " not found"));
        return sessionPlayerRepository.findBySession(session).stream()
                .map(sp -> new UserPayload(sp.getUser().getUsername(), sp.getUser().getEmail(), sessionCode))
                .collect(Collectors.toList());
    }
}