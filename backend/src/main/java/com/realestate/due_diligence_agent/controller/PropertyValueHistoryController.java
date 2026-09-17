package com.realestate.due_diligence_agent.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.realestate.due_diligence_agent.service.PropertyValueHistoryService;
import com.realestate.due_diligence_agent.service.PropertyValueHistoryService.ValueHistoryEntry;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyValueHistoryController {

    private final PropertyValueHistoryService propertyValueHistoryService;

    @GetMapping("/{id}/value-history")
    public ResponseEntity<List<ValueHistoryEntry>> getValueHistory(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                propertyValueHistoryService.getValueHistory(id)
        );
    }
}