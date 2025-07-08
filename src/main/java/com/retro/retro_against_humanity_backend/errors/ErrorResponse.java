package com.retro.retro_against_humanity_backend.errors;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Value;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Value
public class ErrorResponse {
    LocalDateTime timestamp = LocalDateTime.now();
    String error;
    String path;
    Object details;
}