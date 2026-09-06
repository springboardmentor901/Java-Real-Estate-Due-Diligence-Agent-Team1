package com.realestate.due_diligence_agent.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.realestate.due_diligence_agent.entity.ZoningInformation;
import com.realestate.due_diligence_agent.service.ZoningService;

@RestController
@RequestMapping("/api/properties")
public class ZoningController {

    private final ZoningService zoningService;


    public ZoningController(ZoningService zoningService) {

        this.zoningService = zoningService;
    }


    @GetMapping("/{id}/zoning")
    public ResponseEntity<?> getZoning(
            @PathVariable Long id) {

        ZoningInformation zoningInformation =
                zoningService.getZoningInformation(id);


        if (zoningInformation == null) {

            return ResponseEntity.ok(
                    java.util.Map.of(
                            "propertyId", id,
                            "message",
                            "No zoning information found from Regrid for this property"
                    )
            );
        }


        return ResponseEntity.ok(zoningInformation);
    }
}