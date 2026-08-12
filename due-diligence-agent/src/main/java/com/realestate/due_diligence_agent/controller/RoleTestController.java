package com.realestate.due_diligence_agent.controller;
 
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
@RestController
@RequestMapping("/api/test")
public class RoleTestController {
 
    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public String adminOnly() {
        return "Reached - you are an ADMINISTRATOR.";
    }
 
    @GetMapping("/agent-only")
    @PreAuthorize("hasRole('REAL_ESTATE_AGENT')")
    public String agentOnly() {
        return "Reached - you are a REAL_ESTATE_AGENT.";
    }
 
    @GetMapping("/legal-only")
    @PreAuthorize("hasRole('LEGAL_REVIEWER')")
    public String legalOnly() {
        return "Reached - you are a LEGAL_REVIEWER.";
    }
 
    @GetMapping("/bank-only")
    @PreAuthorize("hasRole('FINANCIAL_INSTITUTION')")
    public String bankOnly() {
        return "Reached - you are a FINANCIAL_INSTITUTION.";
    }
 
    @GetMapping("/buyer-or-agent")
    @PreAuthorize("hasAnyRole('BUYER','REAL_ESTATE_AGENT')")
    public String buyerOrAgent() {
        return "Reached - you are a BUYER or REAL_ESTATE_AGENT.";
    }
}
