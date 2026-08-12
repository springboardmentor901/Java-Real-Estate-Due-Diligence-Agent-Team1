package com.realestate.due_diligence_agent.controller;

import com.realestate.due_diligence_agent.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/secure")
    public String secure(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        return "You reached a protected endpoint! Logged in as: "
                + user.getFullName() + " (" + user.getEmail() + "), role: " + user.getRole();
    }
}