package com.haucky.chat.service;

import com.haucky.chat.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TicketService {
    private final Map<Long, Ticket> ticketsMap = new ConcurrentHashMap<>();
    private final AtomicLong ticketIdSequence = new AtomicLong(1);

    private final UserService userService;
    private final RatingService ratingService;

    public TicketService(UserService userService, RatingService ratingService) {
        this.userService = userService;
        this.ratingService = ratingService;
    }

    public Ticket createTicket(Long customerId, String content) {
        User customer = userService.getUserById(customerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (customer.isAgent()) {
            throw new RuntimeException("Agents cannot create tickets");
        }

        User agent = userService.getDefaultAgent();

        Ticket ticket = Ticket.builder()
                .id(ticketIdSequence.getAndIncrement())
                .customer(customer)
                .agent(agent)
                .resolved(false)
                .createdAt(LocalDateTime.now()) // Set creation timestamp
                .messages(new ArrayList<>())
                .build();

        if (content != null && !content.isEmpty()) {
            ticket.addMessage(customer, content);
        }

        customer.getTickets().add(ticket);
        agent.getTickets().add(ticket);

        ticketsMap.put(ticket.getId(), ticket);

        return ticket;
    }

    public List<Ticket> getTicketsByCustomerId(Long customerId) {
        return ticketsMap.values().stream()
                .filter(ticket -> ticket.getCustomer().getId().equals(customerId))
                .toList();
    }

    public List<Ticket> getTicketsByAgentId(Long agentId) {
        return ticketsMap.values().stream()
                .filter(ticket -> ticket.getAgent().getId().equals(agentId))
                .toList();
    }

    public Optional<Ticket> getTicketById(Long ticketId) {
        return Optional.ofNullable(ticketsMap.get(ticketId));
    }

    public Message addMessage(Long ticketId, User sender, String content) {
        Ticket ticket = getTicketById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if(ticket.isResolved()){
            throw new RuntimeException("Ticket is already resolved");
        }

        Message message = ticket.addMessage(sender, content);
        ticketsMap.put(ticket.getId(), ticket);
        return message;
    }

    public Ticket resolveTicket(Long ticketId) {
        Ticket ticket = getTicketById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ratingService.evaluateTicket(ticket);
        ticket.setResolved(true);

        ticketsMap.put(ticket.getId(), ticket);
        return ticket;
    }
}