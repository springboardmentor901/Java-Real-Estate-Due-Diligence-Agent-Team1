package com.realestate.due_diligence_agent.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.realestate.due_diligence_agent.entity.FloodZoneData;
import com.realestate.due_diligence_agent.service.FloodZoneService;

@RestController
@RequestMapping("/api/properties")
public class FloodZoneController {

    private final FloodZoneService floodZoneService;

    public FloodZoneController(
            FloodZoneService floodZoneService) {

        this.floodZoneService = floodZoneService;
    }

    @GetMapping("/{id}/flood-zone")
    public ResponseEntity<FloodZoneData> getFloodZone(
            @PathVariable Long id) {

        FloodZoneData floodZoneData =
                floodZoneService.getFloodZoneData(id);

        return ResponseEntity.ok(floodZoneData);
    }
}