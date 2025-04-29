package com.haucky.chat.controller.view;

import com.haucky.chat.domain.Ticket;
import com.haucky.chat.domain.User;
import com.haucky.chat.service.TicketService;
import com.haucky.chat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller responsible for rendering ticket-related views and fragments
 */
@Controller
@RequestMapping("/tickets")
public class TicketViewController {

    private final TicketService ticketService;
    private final UserService userService;

    @Autowired
    public TicketViewController(TicketService ticketService, UserService userService) {
        this.ticketService = ticketService;
        this.userService = userService;
    }

    @GetMapping("/self")
    public String getTickets(@RequestParam(required = false) Boolean resolved, Authentication authentication, Model model) {
        User currentUser = findCurrentUser(authentication);
        if (currentUser == null) {
            return "fragments/ticket :: errorFragment";
        }

        List<Ticket> tickets;
        if (currentUser.isAgent()) {
            tickets = ticketService.getTicketsByAgentId(currentUser.getId());
        } else {
            tickets = ticketService.getTicketsByCustomerId(currentUser.getId());
        }

        if (resolved != null) {
            tickets = tickets.stream()
                    .filter(ticket -> ticket.isResolved() == resolved)
                    .toList();
        }

        model.addAttribute("tickets", tickets);
        return "fragments/ticket :: ticketsList";
    }

    @PostMapping("/view/create")
    public String createTicketView(@RequestParam String message, Authentication authentication, Model model) {
        User currentUser = findCurrentUser(authentication);

        if (currentUser != null && !currentUser.isAgent()) {
            Ticket ticket = ticketService.createTicket(currentUser.getId(), message);
            model.addAttribute("ticket", ticket);
            return "fragments/ticket :: ticketItem";
        } else {
            return "fragments/ticket :: errorFragment";
        }
    }

    @PostMapping("/{ticketId}/view/resolve")
    public String resolveTicketView(@PathVariable Long ticketId, Model model) {
        Ticket resolvedTicket = ticketService.resolveTicket(ticketId);
        model.addAttribute("ticket", resolvedTicket);

        return "fragments/ticket :: resolvedTicketItem";
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