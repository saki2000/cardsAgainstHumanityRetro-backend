package com.retro.retro_against_humanity_backend.Controllers;

import com.retro.retro_against_humanity_backend.Service.SessionService;
import com.retro.retro_against_humanity_backend.dto.SessionCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
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
    public ResponseEntity<String> checkSession(@PathVariable String sessionId) {
        // Implement your session check logic here
        return ResponseEntity.ok("Session checked: " + sessionId);
    }
};


