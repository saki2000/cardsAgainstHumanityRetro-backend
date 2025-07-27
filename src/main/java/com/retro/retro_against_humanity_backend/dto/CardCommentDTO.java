package com.retro.retro_against_humanity_backend.dto;

import com.retro.retro_against_humanity_backend.entity.Card;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardCommentDTO {
    private String cardContent;
    private Card.CardType cardType;
    private List<String> comments;
}
