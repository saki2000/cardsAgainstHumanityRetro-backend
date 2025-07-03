package com.retro.retro_against_humanity_backend.Controllers;

import com.retro.retro_against_humanity_backend.Service.SessionService;
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

    @GetMapping("/check/{sessionId}")
    public ResponseEntity<String> checkSession(
            @PathVariable
            @Size(min = 6, max = 6, message = "Session ID must be exactly 6 characters")
            @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Session ID must be alphanumeric")
            String sessionId) {
        sessionService.checkActiveSessions(sessionId);
        return ResponseEntity.ok(sessionId);
    }
};


