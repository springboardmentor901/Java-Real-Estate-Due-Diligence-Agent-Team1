package com.realestate.due_diligence_agent.controller;

import com.realestate.due_diligence_agent.dto.PropertyTimelineEntry;
import com.realestate.due_diligence_agent.service.PropertyHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyHistoryController {

    private final PropertyHistoryService propertyHistoryService;

    @GetMapping("/{id}/history")
    public ResponseEntity<List<PropertyTimelineEntry>> getPropertyHistory(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                propertyHistoryService.getPropertyHistory(id)
        );
    }
}