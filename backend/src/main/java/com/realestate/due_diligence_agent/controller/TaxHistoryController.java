package com.realestate.due_diligence_agent.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.realestate.due_diligence_agent.entity.TaxHistory;
import com.realestate.due_diligence_agent.service.TaxHistoryService;

@RestController
@RequestMapping("/api/properties")
public class TaxHistoryController {

    private final TaxHistoryService taxHistoryService;

    public TaxHistoryController(TaxHistoryService taxHistoryService) {
        this.taxHistoryService = taxHistoryService;
    }

    @GetMapping("/{id}/tax-history")
    public ResponseEntity<List<TaxHistory>> getTaxHistory(
            @PathVariable Long id) {

        List<TaxHistory> taxHistory =
                taxHistoryService.getTaxHistory(id);

        return ResponseEntity.ok(taxHistory);
    }
}