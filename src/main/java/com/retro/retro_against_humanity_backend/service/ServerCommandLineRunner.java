package com.retro.retro_against_humanity_backend.service;

import com.corundumstudio.socketio.SocketIOServer;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ServerCommandLineRunner implements CommandLineRunner {

    private final SocketIOServer server;

    public ServerCommandLineRunner(SocketIOServer server) {
        this.server = server;
    }

    @Override
    public void run(String... args) throws Exception {
        server.start();
    }

    @PreDestroy
    public void stopServer() {
        server.stop();
    }
}
