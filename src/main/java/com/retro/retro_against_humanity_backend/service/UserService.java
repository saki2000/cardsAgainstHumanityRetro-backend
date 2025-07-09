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

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public Users findOrCreateUser(String username, String email) {
        return userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.save(
                        new Users(null, email, username, 0)
                ));
    }
}