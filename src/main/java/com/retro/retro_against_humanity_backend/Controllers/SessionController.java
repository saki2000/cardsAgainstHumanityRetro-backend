package com.retro.retro_against_humanity_backend.Controllers;

import com.retro.retro_against_humanity_backend.Service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping("/create")
    public ResponseEntity<String> createSession() {
        String sessionId = sessionService.create();
        return ResponseEntity.ok(sessionId);
    }
}


