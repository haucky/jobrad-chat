package com.haucky.chat.controller.view;

import com.haucky.chat.domain.*;
import com.haucky.chat.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

/**
 * Controller responsible for rendering chat-related views
 */
@Controller
public class ChatViewController {

    private final TicketService ticketService;

    @Autowired
    public ChatViewController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/chat/{ticketId}")
    public String chatPage(@PathVariable Long ticketId, Model model, Authentication authentication) {
        Optional<Ticket> ticketOpt = ticketService.getTicketById(ticketId);
        
        if (ticketOpt.isEmpty()) {
            return "redirect:/";
        }

        Ticket ticket = ticketOpt.get();
        model.addAttribute("ticket", ticket);
        
        return "chat";
    }
}