package com.retro.retro_against_humanity_backend.controllers;

import com.retro.retro_against_humanity_backend.dto.UserNameRequest;
import com.retro.retro_against_humanity_backend.entity.Users;
import com.retro.retro_against_humanity_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping("/retrieve")
    public ResponseEntity<Users> userDetails(@Valid @RequestBody UserNameRequest request) {

        Users userData = userService.getUser(request.getUserName());
        return ResponseEntity.ok(userData);
    }
}
