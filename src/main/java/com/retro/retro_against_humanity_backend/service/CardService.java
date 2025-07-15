package com.retro.retro_against_humanity_backend.service;

import com.retro.retro_against_humanity_backend.dto.CardDto;
import com.retro.retro_against_humanity_backend.entity.*;
import com.retro.retro_against_humanity_backend.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final SessionCardRepository sessionCardRepository;
    private final ActiveSessionRepository activeSessionRepository;
    private final SessionPlayerRepository sessionPlayerRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public void clearPlayedCardSlots(String sessionCode) {
        List<SessionCard> playedCards = sessionCardRepository.findPlayedCardsInSession(sessionCode);
        for (SessionCard sessionCard : playedCards) {
            sessionCard.setSlotId(null);
        }
        sessionCardRepository.saveAll(playedCards);
    }

    @Transactional
    public List<CardDto> getCardsForNextRound(String sessionCode, int numberOfCards) {
        List<Card> availableCards = cardRepository.findAvailableCards(sessionCode);
        Collections.shuffle(availableCards);
        List<Card> cardsToDeal = availableCards.stream().limit(numberOfCards).toList();
        return cardsToDeal.stream()
                .map(card -> CardDto.builder()
                        .id(card.getId())
                        .content(card.getContent())
                        .type(card.getType())
                        .build())
                .toList();
    }

    @Transactional
    public void playCard(String sessionCode, Long cardId, String slotId) {
        ActiveSession session = activeSessionRepository.findByCode(sessionCode)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionCode));
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found: " + cardId));

        SessionCard sessionCard = new SessionCard();
        sessionCard.setSession(session);
        sessionCard.setCard(card);
        sessionCard.setSlotId(slotId);

        sessionCardRepository.save(sessionCard);
    }

   @Transactional
   public Map<String, CardDto> getPlayedCardsForRound(String sessionCode) {
       List<SessionCard> playedCards = sessionCardRepository.findPlayedCardsInSession(sessionCode);

       return playedCards.stream()
               .collect(Collectors.toMap(
                       SessionCard::getSlotId,
                       sc -> new CardDto(
                               sc.getCard().getId(),
                               sc.getCard().getContent(),
                               sc.getCard().getType()
                       )
               ));
   }

    @Transactional
    public void submitComment(String sessionCode, long sessionCardId, String commentText, Long authorUserId) {
        SessionPlayer authorPlayer = sessionPlayerRepository.findBySessionCodeAndUserId(sessionCode, authorUserId)
                .orElseThrow(() -> new EntityNotFoundException("Player not found in session"));
        SessionCard sessionCard = sessionCardRepository.findById(sessionCardId)
                .orElseThrow(() -> new EntityNotFoundException("Played card not found"));

        Comment comment = new Comment();
        comment.setSessionCard(sessionCard);
        comment.setAuthorPlayer(authorPlayer);
        comment.setContent(commentText);
        commentRepository.save(comment);
    }
}
