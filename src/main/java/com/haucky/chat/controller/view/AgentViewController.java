package com.haucky.chat.controller.view;

import com.haucky.chat.domain.User;
import com.haucky.chat.domain.RatingResult;
import com.haucky.chat.service.UserService;
import com.haucky.chat.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Controller responsible for rendering agent views
 */
@Controller
public class AgentViewController {

    private final UserService userService;
    private final RatingService ratingService;

    @Autowired
    public AgentViewController(UserService userService, RatingService ratingService) {
        this.userService = userService;
        this.ratingService = ratingService;
    }

    @GetMapping("/agent/self/rating")
    public String rating(Model model, Authentication authentication) {
        User currentAgent = findCurrentAgent(authentication);
        if (currentAgent == null) {
            return "redirect:/";
        }

        List<RatingResult> ratings = ratingService.getAgentRatings(currentAgent.getId());
        model.addAttribute("agent", currentAgent);
        model.addAttribute("ratings", ratings);

        return "fragments/rating :: ratingContent";
    }

    private User findCurrentAgent(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        return userService.getAllUsers().stream()
                .filter(User::isAgent)
                .filter(user -> user.getUsername().equals(authentication.getName()))
                .findFirst()
                .orElse(null);
    }
}