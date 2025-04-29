package com.haucky.chat.controller.api;

import com.haucky.chat.domain.Message;
import com.haucky.chat.domain.User;
import com.haucky.chat.service.MessageNotificationService;
import com.haucky.chat.service.TicketService;
import com.haucky.chat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for posting new messages
 */
@RestController
@RequestMapping("/api/tickets/{ticketId}/messages")
public class MessageController {

    private final TicketService ticketService;
    private final UserService userService;
    private final MessageNotificationService messageNotificationService;

    @Autowired
    public MessageController(TicketService ticketService, UserService userService,
                             MessageNotificationService messageNotificationService) {
        this.ticketService = ticketService;
        this.userService = userService;
        this.messageNotificationService = messageNotificationService;
    }

    @PostMapping
    public ResponseEntity<Void> addMessage(@PathVariable Long ticketId,
                                           @RequestParam String content,
                                           Authentication authentication) {
        User sender = findCurrentUser(authentication);
        if (sender == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Message addedMessage = ticketService.addMessage(ticketId, sender, content);
        messageNotificationService.notifyNewMessage(ticketId, addedMessage);
        return ResponseEntity.noContent().build();
    }

    private User findCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        return userService.getAllUsers().stream()
                .filter(user -> user.getUsername().equals(authentication.getName()))
                .findFirst()
                .orElse(null);
    }
}