package com.haucky.chat.config;

import com.haucky.chat.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    @Value("${chat.default-password}")
    private String defaultPassword;

    private final UserService userService;

    public WebSecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/css/**", "/js/**").permitAll()
                        .requestMatchers("/login", "/register").permitAll()

                        // Stream API - authenticated users only
                        .requestMatchers("/api/stream/**").authenticated()

                        // Agent-specific access
                        .requestMatchers("/agent/**").hasRole("AGENT")
                        .requestMatchers(HttpMethod.GET, "/agent/self/rating").hasRole("AGENT")
                        .requestMatchers(HttpMethod.POST, "/tickets/*/view/resolve").hasRole("AGENT")
                        .requestMatchers(HttpMethod.POST, "/api/tickets/*/resolve").hasRole("AGENT")

                        // Customer-specific access
                        .requestMatchers(HttpMethod.POST, "/tickets/view/create").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/tickets").hasRole("CUSTOMER")

                        // Shared access for viewing tickets
                        .requestMatchers(HttpMethod.GET, "/tickets/self").authenticated()
                        .requestMatchers(HttpMethod.GET, "/chat/*").authenticated()

                        // Both agents and customers can post messages
                        .requestMatchers(HttpMethod.POST, "/api/tickets/*/messages").authenticated()

                        // Default - require authentication for everything else
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .successHandler((request, response, authentication) -> {
                            if (authentication.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_AGENT"))) {
                                response.sendRedirect("/agent");
                            } else {
                                response.sendRedirect("/");
                            }
                        })
                        .permitAll()
                )
                .logout((logout) -> logout
                        .permitAll()
                        .logoutSuccessUrl("/login?logout")
                );
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        List<UserDetails> userDetailsList = new ArrayList<>();
        List<com.haucky.chat.domain.User> domainUsers = userService.getAllUsers();
        for (com.haucky.chat.domain.User domainUser : domainUsers) {
            String role = domainUser.isAgent() ? "AGENT" : "CUSTOMER";

            UserDetails userDetails = User.builder()
                    .username(domainUser.getUsername())
                    .password(passwordEncoder.encode(defaultPassword))
                    .roles(role)
                    .build();
            userDetailsList.add(userDetails);
        }
        return new InMemoryUserDetailsManager(userDetailsList);
    }
}