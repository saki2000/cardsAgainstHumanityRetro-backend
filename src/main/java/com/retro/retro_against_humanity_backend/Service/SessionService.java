package com.retro.retro_against_humanity_backend.Service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SessionService {
    public String create() {
        String sessionId = UUID.randomUUID().toString().substring(0, 6);

        return sessionId;
    }
}
