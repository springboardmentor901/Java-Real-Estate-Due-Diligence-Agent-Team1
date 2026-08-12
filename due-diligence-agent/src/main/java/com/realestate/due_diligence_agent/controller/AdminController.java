package com.realestate.due_diligence_agent.controller;
 
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
@RestController
@RequestMapping("/api/admin")
public class AdminController {
 
    @GetMapping("/dashboard")
    public String dashboard() {
        return "Admin dashboard reached - protected purely by the /api/admin/** rule in SecurityConfig.";
    }
}
