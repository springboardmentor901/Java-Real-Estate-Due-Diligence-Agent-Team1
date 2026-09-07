package com.realestate.due_diligence_agent.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.realestate.due_diligence_agent.entity.UtilityInformation;
import com.realestate.due_diligence_agent.service.UtilityInformationService;

@RestController
@RequestMapping("/api/properties/{propertyId}/utilities")
public class UtilityInformationController {

    private final UtilityInformationService utilityInformationService;

    public UtilityInformationController(
            UtilityInformationService utilityInformationService) {

        this.utilityInformationService = utilityInformationService;
    }

    // =========================================================
    // CREATE
    // POST /api/properties/{propertyId}/utilities
    // =========================================================

    @PostMapping
    public ResponseEntity<UtilityInformation> createUtility(
            @PathVariable Long propertyId,
            @RequestBody UtilityInformation utilityInformation) {

        return ResponseEntity.ok(
                utilityInformationService.createUtility(
                        propertyId,
                        utilityInformation
                )
        );
    }

    // =========================================================
    // GET ALL
    // GET /api/properties/{propertyId}/utilities
    // =========================================================

    @GetMapping
    public ResponseEntity<List<UtilityInformation>> getUtilities(
            @PathVariable Long propertyId) {

        return ResponseEntity.ok(
                utilityInformationService.getUtilities(
                        propertyId
                )
        );
    }

    // =========================================================
    // UPDATE
    // PUT /api/properties/{propertyId}/utilities/{utilityId}
    // =========================================================

    @PutMapping("/{utilityId}")
    public ResponseEntity<UtilityInformation> updateUtility(
            @PathVariable Long propertyId,
            @PathVariable Long utilityId,
            @RequestBody UtilityInformation utilityInformation) {

        return ResponseEntity.ok(
                utilityInformationService.updateUtility(
                        propertyId,
                        utilityId,
                        utilityInformation
                )
        );
    }

    // =========================================================
    // DELETE
    // DELETE /api/properties/{propertyId}/utilities/{utilityId}
    // =========================================================

    @DeleteMapping("/{utilityId}")
    public ResponseEntity<Void> deleteUtility(
            @PathVariable Long propertyId,
            @PathVariable Long utilityId) {

        utilityInformationService.deleteUtility(
                propertyId,
                utilityId
        );

        return ResponseEntity.noContent().build();
    }
}