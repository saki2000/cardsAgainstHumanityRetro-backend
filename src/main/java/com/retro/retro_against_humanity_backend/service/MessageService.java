package com.retro.retro_against_humanity_backend.service;

import com.retro.retro_against_humanity_backend.dto.CardCommentDTO;
import com.retro.retro_against_humanity_backend.entity.ActiveSession;
import com.retro.retro_against_humanity_backend.entity.Card;
import com.retro.retro_against_humanity_backend.repository.CommentRepository;
import com.retro.retro_against_humanity_backend.repository.SessionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final CommentRepository commentRepository;
    private final SessionHistoryRepository sessionHistoryRepository;
    private final SesV2Client sesClient;

    private static final String SUBJECT = "Your Retro Game Summary!";
    private static final String FROM_EMAIL = "noreply.retrogame@gmail.com";


    public void sendResults(ActiveSession session){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formattedDate = session.getCreatedAt().format(formatter);

        List<String> emails = getUserEmails(session);
        List<Object[]> rawCardCommentData =  getCardsAndComments(session.getId());

        Map<Card.CardType, List<CardCommentDTO>> groupedCommentsByType = groupCommentsByCardType(rawCardCommentData);
        String summaryHTML = formatEmailBody(groupedCommentsByType, session.getCode(), formattedDate);

        sendEmails(emails, summaryHTML);
    }

    private List<String> getUserEmails(ActiveSession session ) {
        return sessionHistoryRepository.findUserEmailsBySessionId(session.getId());
    }

    private List<Object[]> getCardsAndComments(Long sessionId) {
        return commentRepository.findCardContentTypeAndCommentBySessionId(sessionId);
    }

    private Map<Card.CardType, List<CardCommentDTO>> groupCommentsByCardType(List<Object[]> rawCardCommentData) {
        Map<Card.CardType, List<Object[]>> dataByCardType = rawCardCommentData.stream()
                .collect(Collectors.groupingBy(row -> (Card.CardType) row[1]));

        Map<Card.CardType, List<CardCommentDTO>> result = new LinkedHashMap<>();

        for (Card.CardType type : Card.CardType.values()) {
            List<Object[]> typeData = dataByCardType.getOrDefault(type, new ArrayList<>());

            Map<String, List<String>> commentsByCardContent = typeData.stream()
                    .collect(Collectors.groupingBy(
                            row -> (String) row[0],
                            Collectors.mapping(row -> (String) row[2], Collectors.toList())
                    ));

            List<CardCommentDTO> dtoListForType = commentsByCardContent.entrySet().stream()
                    .map(entry -> CardCommentDTO.builder()
                            .cardContent(entry.getKey())
                            .cardType(type)
                            .comments(entry.getValue())
                            .build())
                    .sorted(Comparator.comparing(CardCommentDTO::getCardContent))
                    .collect(Collectors.toList());

            if (!dtoListForType.isEmpty()) {
                result.put(type, dtoListForType);
            }
        }
        return result;
    }

    private String formatEmailBody(Map<Card.CardType, List<CardCommentDTO>> groupedCardComments,
                                   String sessionCode, String date) {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<html><body>");
        htmlBuilder.append("<h1>Retro Game Summary</h1>");
        htmlBuilder.append("<h2>").append("Session number: ")
                .append(sessionCode)
                .append(" date: ")
                .append(date)
                .append("</h2>");


        List<Card.CardType> orderedTypes = List.of(Card.CardType.GOOD, Card.CardType.BAD, Card.CardType.OTHER);

        for (Card.CardType cardType : orderedTypes) {
            List<CardCommentDTO> cardListForType = groupedCardComments.get(cardType);

            if (cardListForType != null && !cardListForType.isEmpty()) {
                htmlBuilder.append("<h2 style='color:#333; margin-top:20px;'>").append(cardType.name()).append(" Cards</h2>");

                htmlBuilder.append("<table border='1' style='width:100%; border-collapse: collapse; margin-bottom: 20px;'>");
                htmlBuilder.append("<tr style='background-color:#f2f2f2;'>");
                htmlBuilder.append("<th style='padding:8px; border:1px solid #ddd; text-align:left; width:30%;'>Card</th>");
                htmlBuilder.append("<th style='padding:8px; border:1px solid #ddd; text-align:left; width:70%;'>Comments</th>");
                htmlBuilder.append("</tr>");

                for (CardCommentDTO dto : cardListForType) {
                    htmlBuilder.append("<tr>");
                    htmlBuilder.append("<td style='padding:8px; border:1px solid #ddd;'>").append(escapeHtml(dto.getCardContent())).append("</td>");
                    htmlBuilder.append("<td style='padding:8px; border:1px solid #ddd;'>");

                    if (dto.getComments() != null && !dto.getComments().isEmpty()) {
                        htmlBuilder.append("<ul style='margin:0; padding-left:20px;'>");
                        for (String comment : dto.getComments()) {
                            htmlBuilder.append("<li>").append(escapeHtml(comment)).append("</li>");
                        }
                        htmlBuilder.append("</ul>");
                    } else {
                        htmlBuilder.append("<i>No comments</i>");
                    }
                    htmlBuilder.append("</td>");
                    htmlBuilder.append("</tr>");
                }
                htmlBuilder.append("</table>");
            }
        }

        htmlBuilder.append("</body></html>");
        return htmlBuilder.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }


    private void sendEmails(List<String>recipientEmails, String summaryHTML) {
        System.out.println("Sending emails to "+recipientEmails);
        System.out.println("Sending emails\n "+summaryHTML);

        Destination destination = Destination.builder()
                .toAddresses("sakijudo@gmail.com") //  TODO: replace when aws ses allow us to send emails
                .build();

        Content subjectContent = Content.builder().data(SUBJECT).build();
        Content htmlContent = Content.builder().data(summaryHTML).build();

        Body body = Body.builder().html(htmlContent).build();

        Message message = Message.builder()
                .subject(subjectContent)
                .body(body)
                .build();

        EmailContent emailContent = EmailContent.builder()
                .simple(message)
                .build();

        SendEmailRequest request = SendEmailRequest.builder()
                .fromEmailAddress(FROM_EMAIL)
                .destination(destination)
                .content(emailContent)
                .build();

        try {
            sesClient.sendEmail(request);
            System.out.println("Emails sent successfully!");
        } catch (SesV2Exception e) {
            System.err.println("Error sending email via SES: " + e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Failed to send email", e); //TODO: throw a custom exception instead?
        }
    }

}
