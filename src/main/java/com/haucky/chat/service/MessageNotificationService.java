package com.haucky.chat.service;

import com.haucky.chat.domain.Message;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.function.Consumer;

@Service
public class MessageNotificationService {
    // Map of ticket ID to list of message listeners
    private final Map<Long, List<Consumer<Message>>> ticketListeners = new ConcurrentHashMap<>();
    
    public void registerListener(Long ticketId, Consumer<Message> listener) {
        ticketListeners.computeIfAbsent(ticketId, k -> new CopyOnWriteArrayList<>()).add(listener);
    }
    
    public void removeListener(Long ticketId, Consumer<Message> listener) {
        List<Consumer<Message>> listeners = ticketListeners.get(ticketId);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }
    
    public void notifyNewMessage(Long ticketId, Message message) {
        List<Consumer<Message>> listeners = ticketListeners.get(ticketId);
        if (listeners != null) {
            listeners.forEach(listener -> listener.accept(message));
        }
    }
}