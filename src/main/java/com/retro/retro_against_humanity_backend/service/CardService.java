package com.retro.retro_against_humanity_backend.service;

import com.retro.retro_against_humanity_backend.dto.CardDto;
import com.retro.retro_against_humanity_backend.entity.Card;
import com.retro.retro_against_humanity_backend.repository.CardRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {
    CardRepository cardRepository;

    @Transactional
    public List<CardDto> dealCards(String sessionCode, int numberOfCards) {
        List<Card> allAvailableCards = cardRepository.findAvailableCards(sessionCode);
        Collections.shuffle(allAvailableCards);
        List<Card> selectedCards = allAvailableCards.stream()
                .limit(numberOfCards)
                .toList();
        return selectedCards.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private CardDto toDto(Card card) {
        return new CardDto(card.getId(), card.getContent(), card.getType());
    }
}
