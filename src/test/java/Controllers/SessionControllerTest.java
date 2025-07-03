package com.retro.retro_against_humanity_backend.Controllers;

import com.retro.retro_against_humanity_backend.Service.SessionService;
import com.retro.retro_against_humanity_backend.dto.SessionCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SessionController.class)
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
                .andExpect(jsonPath("$.email", is("Invalid email format")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void createSession_shouldReturnBadRequest_whenEmailNull() throws Exception {
        String requestBody = "{ \"email\": null, \"name\": \"Test User\" }";
        mockMvc.perform(post("/session/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email", is("Email cannot be null")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void createSession_shouldReturnBadRequest_whenNameNull() throws Exception {
        String requestBody = "{ \"email\": \"abc@test.com\", \"name\": null }";
        mockMvc.perform(post("/session/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name", is("Name cannot be null")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}