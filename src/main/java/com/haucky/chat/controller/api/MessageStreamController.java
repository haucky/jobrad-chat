package com.haucky.chat.controller.api;

import com.haucky.chat.domain.Message;
import com.haucky.chat.domain.Ticket;
import com.haucky.chat.service.MessageNotificationService;
import com.haucky.chat.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Controller for handling Server-Sent Events for real-time message updates
 */
@RestController
@RequestMapping("/api/stream")
public class MessageStreamController {

    private final TicketService ticketService;
    private final MessageNotificationService messageNotificationService;
    private final SpringTemplateEngine templateEngine;

    @Autowired
    public MessageStreamController(TicketService ticketService, MessageNotificationService messageNotificationService,
                              SpringTemplateEngine templateEngine) {
        this.ticketService = ticketService;
        this.messageNotificationService = messageNotificationService;
        this.templateEngine = templateEngine;
    }

    @GetMapping("/tickets/{ticketId}/messages")
    public SseEmitter streamTicketMessages(@PathVariable Long ticketId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        // Get the ticket
        Optional<Ticket> ticketOpt = ticketService.getTicketById(ticketId);
        if (ticketOpt.isEmpty()) {
            emitter.completeWithError(new RuntimeException("Ticket not found"));
            return emitter;
        }

        Consumer<Message> listener = message -> {
            try {
                Context context = new Context();
                context.setVariable("message", message);
                String renderedHtml = templateEngine.process(
                        "fragments/message",
                        Collections.singleton("messageItem"),
                        context
                );
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(renderedHtml));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        };

        messageNotificationService.registerListener(ticketId, listener);

        emitter.onCompletion(() -> messageNotificationService.removeListener(ticketId, listener));
        emitter.onTimeout(() -> messageNotificationService.removeListener(ticketId, listener));
        emitter.onError(e -> messageNotificationService.removeListener(ticketId, listener));

        return emitter;
    }
}