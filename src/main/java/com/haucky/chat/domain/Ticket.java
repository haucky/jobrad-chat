package com.haucky.chat.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Ticket {
    private Long id;
    private LocalDateTime createdAt; // Added timestamp for ticket creation
    private User customer;
    private User agent;

    private boolean resolved = false; // Initially not resolved

    @Setter(AccessLevel.NONE)
    private List<Message> messages = new ArrayList<>();

    public Message addMessage(User sender, String content) {
        Message message = Message.builder()
                .id(UUID.randomUUID().toString())
                .sender(sender)
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();
        messages.add(message);
        return message;
    }
}