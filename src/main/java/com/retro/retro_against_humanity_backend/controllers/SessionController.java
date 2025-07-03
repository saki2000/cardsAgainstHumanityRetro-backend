package com.retro.retro_against_humanity_backend.controllers;

import com.retro.retro_against_humanity_backend.errors.Constants;
import com.retro.retro_against_humanity_backend.service.SessionService;
import com.retro.retro_against_humanity_backend.dto.SessionCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
@Validated
public class SessionController {

    private final SessionService sessionService;

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    @PostMapping("/create")
    public ResponseEntity<String> createSession(@Valid @RequestBody SessionCreateRequest request) {
        String sessionId = sessionService.create(request);
        return ResponseEntity.ok(sessionId);
    }

    @GetMapping("/check/{sessionCode}")
    public ResponseEntity<String> checkSession(
            @PathVariable
            @Size(min = Constants.Session.SESSION_ID_MIN_LENGTH, max = Constants.Session.SESSION_ID_MAX_LENGTH, message = Constants.Session.SESSION_CODE_SIZE_MESSAGE)
            @Pattern(regexp = Constants.Session.SESSION_ID_PATTERN, message = Constants.Session.SESSION_CODE_PATTERN_MESSAGE)
            String sessionCode) {
        sessionService.checkActiveSessions(sessionCode);
        return ResponseEntity.ok(sessionCode);
    }

    @DeleteMapping("delete/{sessionCode}")
    public ResponseEntity<Void> deleteSession(
            @PathVariable
            @Size(min = Constants.Session.SESSION_ID_MIN_LENGTH, max = Constants.Session.SESSION_ID_MAX_LENGTH, message = Constants.Session.SESSION_CODE_SIZE_MESSAGE)
            @Pattern(regexp = Constants.Session.SESSION_ID_PATTERN, message = Constants.Session.SESSION_CODE_PATTERN_MESSAGE)
            String sessionCode) {
        sessionService.deleteSession(sessionCode);
        return ResponseEntity.noContent().build();
    }
};


