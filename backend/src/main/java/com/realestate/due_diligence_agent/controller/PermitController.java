package com.realestate.due_diligence_agent.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.realestate.due_diligence_agent.entity.Permit;
import com.realestate.due_diligence_agent.service.PermitService;

@RestController
@RequestMapping("/api/properties")
public class PermitController {

    private final PermitService permitService;

    public PermitController(PermitService permitService) {
        this.permitService = permitService;
    }

    @GetMapping("/{id}/permits")
    public ResponseEntity<List<Permit>> getPermits(
            @PathVariable Long id) {

        List<Permit> permits =
                permitService.getPermits(id);

        return ResponseEntity.ok(permits);
    }
}