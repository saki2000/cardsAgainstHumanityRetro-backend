package com.retro.retro_against_humanity_backend.service;

import com.retro.retro_against_humanity_backend.dto.CardCommentDTO;
import com.retro.retro_against_humanity_backend.entity.ActiveSession;
import com.retro.retro_against_humanity_backend.repository.CardRepository;
import com.retro.retro_against_humanity_backend.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final CommentRepository commentRepository;

    public void sendResults(ActiveSession session){

        List<CardCommentDTO> cardCommentDTOList =  getCardsAndComments(session.getId());

        String test = "test";
        System.out.println("Sending results for session: " + cardCommentDTOList);
    }

    private List<CardCommentDTO> getCardsAndComments(Long sessionId) {
        return commentRepository.findCardContentAndCommentsBySessionId(sessionId);
    }
}
