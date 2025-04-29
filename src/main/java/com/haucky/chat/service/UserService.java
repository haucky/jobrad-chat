package com.haucky.chat.service;

import com.haucky.chat.domain.User;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {
    private final Map<Long, User> usersMap = new ConcurrentHashMap<>();
    private final AtomicLong userIdSequence = new AtomicLong(1);

    @Getter
    private User defaultAgent;

    public UserService(
            @Value("#{'${chat.customer-names}'.split(',')}") List<String> customerNames,
            @Value("${chat.default-agent}") String defaultAgentName) {
        
        // Create regular users (customers)
        for (String customerName : customerNames) {
            createUser(customerName, false);
        }
        
        // Create the default agent
        this.defaultAgent = createUser(defaultAgentName, true);
    }

    public User createUser(String username, boolean isAgent) {
        User user = User.builder()
                .id(userIdSequence.getAndIncrement())
                .username(username)
                .agent(isAgent)
                .build();

        usersMap.put(user.getId(), user);
        return user;
    }

    public Optional<User> getUserById(Long id) {
        return Optional.ofNullable(usersMap.get(id));
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(usersMap.values());
    }
    
    public List<User> getAllCustomers() {
        return usersMap.values().stream()
                .filter(user -> !user.isAgent())
                .toList();
    }
    
    public List<User> getAllAgents() {
        return usersMap.values().stream()
                .filter(User::isAgent)
                .toList();
    }
}