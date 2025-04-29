package com.haucky.chat.domain;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {
    private String id;
    @ToString.Exclude
    private User sender;
    private String content;
    private LocalDateTime timestamp;
}