package com.realestate.due_diligence_agent.controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.realestate.due_diligence_agent.dto.DueDiligenceResponse;
import com.realestate.due_diligence_agent.service.DueDiligenceService;

@RestController
@RequestMapping("/api/properties")
public class DueDiligenceController {

    private final DueDiligenceService dueDiligenceService;

    public DueDiligenceController(
            DueDiligenceService dueDiligenceService) {

        this.dueDiligenceService = dueDiligenceService;
    }

    @GetMapping("/{id}/due-diligence")
    public CompletableFuture<ResponseEntity<DueDiligenceResponse>>
            getDueDiligence(
                    @PathVariable Long id) {

        return dueDiligenceService
                .getDueDiligence(id)
                .thenApply(ResponseEntity::ok);
    }
}