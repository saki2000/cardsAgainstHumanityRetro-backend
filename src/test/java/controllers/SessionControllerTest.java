package com.retro.retro_against_humanity_backend.controllers;

import com.retro.retro_against_humanity_backend.errors.Constants;
import com.retro.retro_against_humanity_backend.dto.SessionCreateRequest;
import com.retro.retro_against_humanity_backend.exceptions.GlobalExceptionHandler;
import com.retro.retro_against_humanity_backend.service.SessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SessionController.class)
@Import(GlobalExceptionHandler.class)
public class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SessionService sessionService;

    private static final String VALID_EMAIL = "test@test.com";
    private static final String INVALID_EMAIL = "invalidEmail";
    private static final String NULL = null;
    private static final String VALID_SESSION_CODE = "abc123";
    private static final String SESSION_CODE_TOO_SHORT = "abc";
    private static final String SESSION_CODE_NOT_ALPHANUMERIC = "abc!@#";
    private static final String USERNAME = "Test User";

    @Test
    void ping_shouldReturnPong() throws Exception {
        mockMvc.perform(get("/session/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));
    }

    @Test
    void createSession_shouldReturnSessionCode() throws Exception {
        String sessionId = "abc123";
        when(sessionService.create(org.mockito.ArgumentMatchers.any(SessionCreateRequest.class)))
                .thenReturn(sessionId);

        String requestBody = "{ \"email\": \"" + VALID_EMAIL + "\", \"name\": \"Test User\" }";

        mockMvc.perform(post("/session/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string(sessionId));
    }

    @Test
    void createSession_shouldReturnBadRequest_whenEmailInvalid() throws Exception {
        String requestBody = "{ \"email\": \"" + INVALID_EMAIL + "\", \"name\": \"" + USERNAME + "\" }";
        mockMvc.perform(post("/session/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Error")))
                .andExpect(jsonPath("$.details.email", is("Invalid email format")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void createSession_shouldReturnBadRequest_whenEmailNull() throws Exception {
        String requestBody = "{ \"email\": " + NULL + ", \"name\": \"" + USERNAME + "\" }";
        mockMvc.perform(post("/session/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Error")))
                .andExpect(jsonPath("$.details.email", is("Email cannot be null")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void createSession_shouldReturnBadRequest_whenNameNull() throws Exception {
        String requestBody = "{ \"email\": \"" + VALID_EMAIL + "\", \"name\": " + NULL + " }";
        mockMvc.perform(post("/session/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Error")))
                .andExpect(jsonPath("$.details.name", is("Name cannot be null")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void checkSession_shouldReturnOk_whenSessionCodeValid() throws Exception {
        mockMvc.perform(get("/session/check/" + VALID_SESSION_CODE))
                .andExpect(status().isOk())
                .andExpect(content().string(VALID_SESSION_CODE));
    }

    @Test
    void checkSession_shouldReturnBadRequest_whenSessionCodeTooShort() throws Exception {
        mockMvc.perform(get("/session/check/" + SESSION_CODE_TOO_SHORT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Constraint Violation")))
                .andExpect(jsonPath("$.details[0]", is(Constants.Session.SESSION_CODE_SIZE_MESSAGE)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void checkSession_shouldReturnBadRequest_whenSessionCodeNotAlphanumeric() throws Exception {
        mockMvc.perform(get("/session/check/" + SESSION_CODE_NOT_ALPHANUMERIC))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Constraint Violation")))
                .andExpect(jsonPath("$.details", hasItem(Constants.Session.SESSION_CODE_PATTERN_MESSAGE)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void deleteSession_shouldReturnNoContent_whenSessionIdValid() throws Exception {
        doNothing().when(sessionService).deleteSession(VALID_SESSION_CODE);

        mockMvc.perform(delete("/session/delete/" + VALID_SESSION_CODE))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteSession_shouldReturnBadRequest_whenSessionCodeTooShort() throws Exception {
        mockMvc.perform(delete("/session/delete/"+ SESSION_CODE_TOO_SHORT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Constraint Violation")))
                .andExpect(jsonPath("$.details[0]", is(Constants.Session.SESSION_CODE_SIZE_MESSAGE)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void deleteSession_shouldReturnBadRequest_whenSessionCodeNotAlphanumeric() throws Exception {
        mockMvc.perform(delete("/session/delete/" + SESSION_CODE_NOT_ALPHANUMERIC))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Constraint Violation")))
                .andExpect(jsonPath("$.details", hasItem(Constants.Session.SESSION_CODE_SIZE_MESSAGE)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}