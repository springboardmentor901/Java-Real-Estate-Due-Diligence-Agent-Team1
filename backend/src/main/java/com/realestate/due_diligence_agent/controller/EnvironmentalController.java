package com.realestate.due_diligence_agent.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.realestate.due_diligence_agent.entity.EnvironmentalRecord;
import com.realestate.due_diligence_agent.service.EnvironmentalService;

@RestController
@RequestMapping("/api/properties")
public class EnvironmentalController {

    private final EnvironmentalService environmentalService;

    public EnvironmentalController(
            EnvironmentalService environmentalService) {

        this.environmentalService =
                environmentalService;
    }


    @GetMapping("/{id}/environmental")
    public ResponseEntity<List<EnvironmentalRecord>>
    getEnvironmentalRecords(
            @PathVariable Long id) {

        List<EnvironmentalRecord> records =
                environmentalService
                        .getEnvironmentalRecords(id);

        return ResponseEntity.ok(records);
    }
}