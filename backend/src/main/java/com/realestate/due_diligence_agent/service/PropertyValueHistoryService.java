package com.realestate.due_diligence_agent.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.entity.TaxHistory;
import com.realestate.due_diligence_agent.repository.PropertyRepository;
import com.realestate.due_diligence_agent.repository.TaxHistoryRepository;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PropertyValueHistoryService {

    private final PropertyRepository propertyRepository;
    private final TaxHistoryRepository taxHistoryRepository;
    private final TaxHistoryService taxHistoryService;

    public List<ValueHistoryEntry> getValueHistory(Long propertyId) {

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Property not found with id: " + propertyId));

        List<TaxHistory> taxHistories = taxHistoryRepository.findByPropertyId(propertyId);

        if (taxHistories.isEmpty()) {
            try {
                taxHistories = taxHistoryService.getTaxHistory(propertyId);
            } catch (Exception ignored) {
                taxHistories = List.of();
            }
        }

        if (taxHistories == null || taxHistories.isEmpty()) {
            return List.of();
        }

        return taxHistories.stream()
                .filter(tax -> tax.getTaxYear() != null)
                .map(tax -> new ValueHistoryEntry(
                        tax.getTaxYear(),
                        tax.getAssessedValue()
                ))
                .sorted(
                        Comparator.comparing(
                                ValueHistoryEntry::getYear
                        )
                )
                .toList();
    }

    @Getter
    @AllArgsConstructor
    public static class ValueHistoryEntry {

        private Integer year;

        private java.math.BigDecimal assessedValue;
    }
}