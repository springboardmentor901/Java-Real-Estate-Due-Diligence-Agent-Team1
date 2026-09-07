package com.realestate.due_diligence_agent.controller;

import com.realestate.due_diligence_agent.dto.DueDiligenceResponse;
import com.realestate.due_diligence_agent.entity.*;
import com.realestate.due_diligence_agent.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/properties/{id}")
public class PropertyDueDiligenceController {

    private final OwnershipService ownershipService;
    private final TaxHistoryService taxHistoryService;
    private final ZoningService zoningService;
    private final FloodZoneService floodZoneService;
    private final PermitsService permitsService;
    private final EnvironmentalService environmentalService;
    private final UtilityInformationService utilityInformationService;

    public PropertyDueDiligenceController(OwnershipService ownershipService,
                                          TaxHistoryService taxHistoryService,
                                          ZoningService zoningService,
                                          FloodZoneService floodZoneService,
                                          PermitsService permitsService,
                                          EnvironmentalService environmentalService,
                                          UtilityInformationService utilityInformationService) {
        this.ownershipService = ownershipService;
        this.taxHistoryService = taxHistoryService;
        this.zoningService = zoningService;
        this.floodZoneService = floodZoneService;
        this.permitsService = permitsService;
        this.environmentalService = environmentalService;
        this.utilityInformationService = utilityInformationService;
    }

    @GetMapping("/ownership")
    public List<OwnershipRecord> getOwnership(@PathVariable Long id) {
        return ownershipService.getOwnershipRecords(id);
    }

    @GetMapping("/tax-history")
    public List<TaxHistory> getTaxHistory(@PathVariable Long id) {
        return taxHistoryService.getTaxHistory(id);
    }

    @GetMapping("/zoning")
    public List<ZoningInformation> getZoning(@PathVariable Long id) {
        return zoningService.getZoningInformation(id);
    }

    @GetMapping("/flood-zone")
    public List<FloodZoneData> getFloodZone(@PathVariable Long id) {
        return floodZoneService.getFloodZoneData(id);
    }

    @GetMapping("/permits")
    public List<Permit> getPermits(@PathVariable Long id) {
        return permitsService.getPermits(id);
    }

    @GetMapping("/environmental")
    public List<EnvironmentalRecord> getEnvironmental(@PathVariable Long id) {
        return environmentalService.getEnvironmentalRecords(id);
    }

    @GetMapping("/utilities")
    public List<UtilityInformation> getUtilities(@PathVariable Long id) {
        return utilityInformationService.getUtilities(id);
    }

    @PostMapping("/utilities")
    public UtilityInformation createUtility(@PathVariable Long id, @RequestBody UtilityInformation utilityInformation, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        return utilityInformationService.createUtility(id, utilityInformation, username);
    }

    @PutMapping("/utilities/{utilityId}")
    public UtilityInformation updateUtility(@PathVariable Long id, @PathVariable Long utilityId, @RequestBody UtilityInformation utilityInformation, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        return utilityInformationService.updateUtility(utilityId, utilityInformation, username);
    }

    @DeleteMapping("/utilities/{utilityId}")
    public ResponseEntity<Void> deleteUtility(@PathVariable Long id, @PathVariable Long utilityId) {
        utilityInformationService.deleteUtility(utilityId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/due-diligence")
    public DueDiligenceResponse getDueDiligence(@PathVariable Long id) throws ExecutionException, InterruptedException {

        CompletableFuture<List<OwnershipRecord>> ownershipFuture = CompletableFuture.supplyAsync(() -> {
            try { return ownershipService.getOwnershipRecords(id); } catch (Exception e) { return null; }
        });

        CompletableFuture<List<TaxHistory>> taxHistoryFuture = CompletableFuture.supplyAsync(() -> {
            try { return taxHistoryService.getTaxHistory(id); } catch (Exception e) { return null; }
        });

        CompletableFuture<List<ZoningInformation>> zoningFuture = CompletableFuture.supplyAsync(() -> {
            try { return zoningService.getZoningInformation(id); } catch (Exception e) { return null; }
        });

        CompletableFuture<List<FloodZoneData>> floodZoneFuture = CompletableFuture.supplyAsync(() -> {
            try { return floodZoneService.getFloodZoneData(id); } catch (Exception e) { return null; }
        });

        CompletableFuture<List<Permit>> permitsFuture = CompletableFuture.supplyAsync(() -> {
            try { return permitsService.getPermits(id); } catch (Exception e) { return null; }
        });

        CompletableFuture<List<EnvironmentalRecord>> environmentalFuture = CompletableFuture.supplyAsync(() -> {
            try { return environmentalService.getEnvironmentalRecords(id); } catch (Exception e) { return null; }
        });

        CompletableFuture<List<UtilityInformation>> utilitiesFuture = CompletableFuture.supplyAsync(() -> {
            try { return utilityInformationService.getUtilities(id); } catch (Exception e) { return null; }
        });

        CompletableFuture.allOf(
                ownershipFuture, taxHistoryFuture, zoningFuture,
                floodZoneFuture, permitsFuture, environmentalFuture, utilitiesFuture
        ).join();

        return DueDiligenceResponse.builder()
                .ownership(ownershipFuture.get())
                .taxHistory(taxHistoryFuture.get())
                .zoning(zoningFuture.get())
                .floodZone(floodZoneFuture.get())
                .permits(permitsFuture.get())
                .environmental(environmentalFuture.get())
                .utilities(utilitiesFuture.get())
                .build();
    }
}
