package com.retro.retro_against_humanity_backend.dto;

import com.retro.retro_against_humanity_backend.entity.Card;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardDto {
    private Long id;
    private String content;
    private Card.CardType type;
}
