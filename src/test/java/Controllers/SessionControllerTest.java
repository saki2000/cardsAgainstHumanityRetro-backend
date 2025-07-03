package com.retro.retro_against_humanity_backend.Controllers;

import com.retro.retro_against_humanity_backend.Exceptions.GlobalExceptionHandler;
import com.retro.retro_against_humanity_backend.Service.SessionService;
import com.retro.retro_against_humanity_backend.dto.SessionCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SessionController.class)
@Import(GlobalExceptionHandler.class)
public class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SessionService sessionService;

    @Test
    void ping_shouldReturnPong() throws Exception {
        mockMvc.perform(get("/session/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));
    }

    @Test
    void createSession_shouldReturnSessionId() throws Exception {
        String sessionId = "abc123";
        when(sessionService.create(org.mockito.ArgumentMatchers.any(SessionCreateRequest.class)))
                .thenReturn(sessionId);

        String requestBody = "{ \"email\": \"abc@test.com\", \"name\": \"Test User\" }";

        mockMvc.perform(post("/session/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string(sessionId));
    }

    @Test
    void createSession_shouldReturnBadRequest_whenEmailInvalid() throws Exception {
        String requestBody = "{ \"email\": \"invalidEmail\", \"name\": \"Test User\" }";
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
        String requestBody = "{ \"email\": null, \"name\": \"Test User\" }";
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
        String requestBody = "{ \"email\": \"abc@test.com\", \"name\": null }";
        mockMvc.perform(post("/session/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Error")))
                .andExpect(jsonPath("$.details.name", is("Name cannot be null")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void checkSession_shouldReturnOk_whenSessionIdValid() throws Exception {
        mockMvc.perform(get("/session/check/abc123"))
                .andExpect(status().isOk())
                .andExpect(content().string("Session checked: abc123"));
    }

    @Test
    void checkSession_shouldReturnBadRequest_whenSessionIdTooShort() throws Exception {
        mockMvc.perform(get("/session/check/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Constraint Violation")))
                .andExpect(jsonPath("$.details[0]", is("Session ID must be exactly 6 characters")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }


    @Test
    void checkSession_shouldReturnBadRequest_whenSessionIdNotAlphanumeric() throws Exception {
        mockMvc.perform(get("/session/check/abc!@#"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Constraint Violation")))
                .andExpect(jsonPath("$.details", hasItem("Session ID must be alphanumeric")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}