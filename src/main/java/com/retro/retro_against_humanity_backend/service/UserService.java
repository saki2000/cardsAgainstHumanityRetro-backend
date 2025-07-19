package com.retro.retro_against_humanity_backend.service;

import com.retro.retro_against_humanity_backend.entity.Users;
import com.retro.retro_against_humanity_backend.errors.Constants;
import com.retro.retro_against_humanity_backend.exceptions.UserNotFoundException;
import com.retro.retro_against_humanity_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Users getUserByUsername(String userName) {
        return userRepository.findByUsername(userName).orElseThrow(()-> new UserNotFoundException(Constants.Users.USER_NOT_FOUND_MESSAGE));
    }
}
