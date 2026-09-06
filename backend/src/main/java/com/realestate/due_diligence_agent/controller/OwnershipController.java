package com.realestate.due_diligence_agent.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.realestate.due_diligence_agent.entity.OwnershipRecord;
import com.realestate.due_diligence_agent.service.OwnershipService;

@RestController
@RequestMapping("/api/properties")
public class OwnershipController {

    private final OwnershipService ownershipService;

    public OwnershipController(OwnershipService ownershipService) {
        this.ownershipService = ownershipService;
    }

    @GetMapping("/{id}/ownership")
    public ResponseEntity<List<OwnershipRecord>> getOwnership(
            @PathVariable Long id) {

        List<OwnershipRecord> records =
                ownershipService.getOwnershipRecords(id);

        return ResponseEntity.ok(records);
    }
}