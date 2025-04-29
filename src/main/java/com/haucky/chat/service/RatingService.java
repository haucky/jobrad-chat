package com.haucky.chat.service;

import com.haucky.chat.domain.Message;
import com.haucky.chat.domain.RatingResult;
import com.haucky.chat.domain.Ticket;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RatingService {
    private final Map<Long, List<RatingResult>> agentRatingsMap = new ConcurrentHashMap<>();

    public RatingResult evaluateTicket(Ticket ticket) {
        int rating = 0;

        if (ticket.getMessages() == null || ticket.getMessages().isEmpty()) {
            return new RatingResult(0, ticket);
        }

        // Rule 1: +1 point if customer posted less than 5 messages
        long customerMessageCount = countUserMessages(ticket.getMessages(), false);
        if (customerMessageCount < 5) {
            rating++;
        }

        // Rule 2: +1 point if agent wrote more than 3 messages
        long agentMessageCount = countUserMessages(ticket.getMessages(), true);
        if (agentMessageCount > 3) {
            rating++;
        }

        RatingResult result = new RatingResult(rating, ticket);
        addRatingForAgent(ticket.getAgent().getId(), result);

        return result;
    }

    private long countUserMessages(List<Message> messages, boolean isAgent) {
        return messages.stream()
                .filter(message -> message.getSender() != null && message.getSender().isAgent() == isAgent)
                .count();
    }

    public void addRatingForAgent(Long agentId, RatingResult rating) {
        agentRatingsMap.computeIfAbsent(agentId, k -> new ArrayList<>()).add(rating);
    }

    public List<RatingResult> getAgentRatings(Long agentId) {
        return agentRatingsMap.getOrDefault(agentId, new ArrayList<>());
    }
}