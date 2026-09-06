package com.realestate.due_diligence_agent.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.stereotype.Service;

import com.realestate.due_diligence_agent.dto.DueDiligenceResponse;
import com.realestate.due_diligence_agent.dto.SectionResult;
import com.realestate.due_diligence_agent.entity.EnvironmentalRecord;
import com.realestate.due_diligence_agent.entity.FloodZoneData;
import com.realestate.due_diligence_agent.entity.OwnershipRecord;
import com.realestate.due_diligence_agent.entity.Permit;
import com.realestate.due_diligence_agent.entity.TaxHistory;
import com.realestate.due_diligence_agent.entity.UtilityInformation;
import com.realestate.due_diligence_agent.entity.ZoningInformation;

@Service
public class DueDiligenceService {

    private final OwnershipService ownershipService;
    private final TaxHistoryService taxHistoryService;
    private final ZoningService zoningService;
    private final FloodZoneService floodZoneService;
    private final PermitService permitService;
    private final EnvironmentalService environmentalService;
    private final UtilityInformationService utilityInformationService;

    private final Executor taskExecutor;

    public DueDiligenceService(
            OwnershipService ownershipService,
            TaxHistoryService taxHistoryService,
            ZoningService zoningService,
            FloodZoneService floodZoneService,
            PermitService permitService,
            EnvironmentalService environmentalService,
            UtilityInformationService utilityInformationService,
            Executor taskExecutor) {

        this.ownershipService = ownershipService;
        this.taxHistoryService = taxHistoryService;
        this.zoningService = zoningService;
        this.floodZoneService = floodZoneService;
        this.permitService = permitService;
        this.environmentalService = environmentalService;
        this.utilityInformationService = utilityInformationService;
        this.taskExecutor = taskExecutor;
    }

    public CompletableFuture<DueDiligenceResponse> getDueDiligence(
            Long propertyId) {

        CompletableFuture<SectionResult<List<OwnershipRecord>>> ownershipFuture =
                CompletableFuture.supplyAsync(
                        () -> {
                            try {
                                return SectionResult.<List<OwnershipRecord>>builder()
                                        .status("SUCCESS")
                                        .data(ownershipService.getOwnershipRecords(propertyId))
                                        .build();
                            } catch (Exception e) {
                                return failedResult(e);
                            }
                        },
                        taskExecutor
                );

        CompletableFuture<SectionResult<List<TaxHistory>>> taxHistoryFuture =
                CompletableFuture.supplyAsync(
                        () -> {
                            try {
                                return SectionResult.<List<TaxHistory>>builder()
                                        .status("SUCCESS")
                                        .data(taxHistoryService.getTaxHistory(propertyId))
                                        .build();
                            } catch (Exception e) {
                                return failedResult(e);
                            }
                        },
                        taskExecutor
                );

        CompletableFuture<SectionResult<ZoningInformation>> zoningFuture =
                CompletableFuture.supplyAsync(
                        () -> {
                            try {
                                return SectionResult.<ZoningInformation>builder()
                                        .status("SUCCESS")
                                        .data(zoningService.getZoningInformation(propertyId))
                                        .build();
                            } catch (Exception e) {
                                return failedResult(e);
                            }
                        },
                        taskExecutor
                );

        CompletableFuture<SectionResult<FloodZoneData>> floodZoneFuture =
                CompletableFuture.supplyAsync(
                        () -> {
                            try {
                                return SectionResult.<FloodZoneData>builder()
                                        .status("SUCCESS")
                                        .data(floodZoneService.getFloodZoneData(propertyId))
                                        .build();
                            } catch (Exception e) {
                                return failedResult(e);
                            }
                        },
                        taskExecutor
                );

        CompletableFuture<SectionResult<List<Permit>>> permitsFuture =
                CompletableFuture.supplyAsync(
                        () -> {
                            try {
                                return SectionResult.<List<Permit>>builder()
                                        .status("SUCCESS")
                                        .data(permitService.getPermits(propertyId))
                                        .build();
                            } catch (Exception e) {
                                return failedResult(e);
                            }
                        },
                        taskExecutor
                );

        CompletableFuture<SectionResult<List<EnvironmentalRecord>>> environmentalFuture =
                CompletableFuture.supplyAsync(
                        () -> {
                            try {
                                return SectionResult.<List<EnvironmentalRecord>>builder()
                                        .status("SUCCESS")
                                        .data(environmentalService.getEnvironmentalRecords(propertyId))
                                        .build();
                            } catch (Exception e) {
                                return failedResult(e);
                            }
                        },
                        taskExecutor
                );

        CompletableFuture<SectionResult<List<UtilityInformation>>> utilitiesFuture =
                CompletableFuture.supplyAsync(
                        () -> {
                            try {
                                return SectionResult.<List<UtilityInformation>>builder()
                                        .status("SUCCESS")
                                        .data(utilityInformationService.getUtilities(propertyId))
                                        .build();
                            } catch (Exception e) {
                                return failedResult(e);
                            }
                        },
                        taskExecutor
                );

        return CompletableFuture.allOf(
                ownershipFuture,
                taxHistoryFuture,
                zoningFuture,
                floodZoneFuture,
                permitsFuture,
                environmentalFuture,
                utilitiesFuture
        ).thenApply(v ->
                DueDiligenceResponse.builder()
                        .ownership(ownershipFuture.join())
                        .taxHistory(taxHistoryFuture.join())
                        .zoning(zoningFuture.join())
                        .floodZone(floodZoneFuture.join())
                        .permits(permitsFuture.join())
                        .environmental(environmentalFuture.join())
                        .utilities(utilitiesFuture.join())
                        .build()
        );
    }

    private <T> SectionResult<T> failedResult(Exception exception) {

        String message = exception.getMessage();

        if (message == null || message.isBlank()) {
            message = exception.getClass().getSimpleName();
        }

        return SectionResult.<T>builder()
                .status("FAILED")
                .data(null)
                .error(message)
                .build();
    }
}