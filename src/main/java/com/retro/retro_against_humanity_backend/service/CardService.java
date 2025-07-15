package com.retro.retro_against_humanity_backend.service;

import com.retro.retro_against_humanity_backend.dto.CardDto;
import com.retro.retro_against_humanity_backend.dto.CommentDto;
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
        final int cardsPerType = numberOfCards / 3; // Got 3 types of cards
        List<Card> cardsToDeal = new ArrayList<>();

        for (Card.CardType type : Card.CardType.values()) {
            List<Card> availableCards = cardRepository.findAvailableCardsByType(sessionCode, type);
            Collections.shuffle(availableCards);
            List<Card> drawnCards = availableCards.stream().limit(cardsPerType).toList();
            cardsToDeal.addAll(drawnCards);
        }

        return cardsToDeal.stream()
                .map(card -> CardDto.builder()
                        .id(card.getId())
                        .sessionCardId(null) // This will be set when the card is played
                        .content(card.getContent())
                        .comments(Collections.emptyList())
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
                       sc -> {
                           List<CommentDto> commentDtos = sc.getComments().stream()
                                   .map(comment -> new CommentDto(
                                           comment.getId(),
                                           comment.getAuthorPlayer().getUser().getUsername(),
                                           comment.getContent(),
                                           0 // TODO: Implement vote counting
                                   ))
                                   .collect(Collectors.toList());

                           return new CardDto(
                                   sc.getCard().getId(),
                                   sc.getId(), // sessionCardId
                                   sc.getCard().getContent(),
                                   commentDtos
                           );
                       }
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
