package com.haucky.chat.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller responsible for authentication and main application views
 */
@Controller
public class AuthController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/")
    public String home() {
        return "customer";
    }
    
    // For demonstration of agent-only page
    @GetMapping("/agent")
    public String agent() {
        return "agent";
    }
}