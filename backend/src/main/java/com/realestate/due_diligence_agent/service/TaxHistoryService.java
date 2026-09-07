package com.realestate.due_diligence_agent.service;

import com.realestate.due_diligence_agent.client.AttomClient;
import com.realestate.due_diligence_agent.dto.AttomAssessmentResponse;
import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.entity.TaxHistory;
import com.realestate.due_diligence_agent.repository.PropertyRepository;
import com.realestate.due_diligence_agent.repository.TaxHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TaxHistoryService {

    private final TaxHistoryRepository taxHistoryRepository;
    private final PropertyRepository propertyRepository;
    private final AttomClient attomClient;

    public TaxHistoryService(TaxHistoryRepository taxHistoryRepository,
                             PropertyRepository propertyRepository,
                             AttomClient attomClient) {
        this.taxHistoryRepository = taxHistoryRepository;
        this.propertyRepository = propertyRepository;
        this.attomClient = attomClient;
    }

    public List<TaxHistory> getTaxHistory(Long propertyId) {
        List<TaxHistory> existing = taxHistoryRepository.findByPropertyId(propertyId);
        if (existing != null && !existing.isEmpty()) {
            return existing;
        }

        Optional<Property> propertyOpt = propertyRepository.findById(propertyId);
        if (propertyOpt.isEmpty()) {
            return new ArrayList<>();
        }

        Property property = propertyOpt.get();
        List<TaxHistory> newRecords = new ArrayList<>();

        try {
            AttomAssessmentResponse response = attomClient.getAssessment(property.getAddress());
            if (response != null && response.getProperty() != null && !response.getProperty().isEmpty()) {
                AttomAssessmentResponse.Property attomProp = response.getProperty().get(0);
                if (attomProp.getAssessment() != null) {
                    TaxHistory record = TaxHistory.builder()
                            .property(property)
                            .taxYear(attomProp.getAssessment().getTax() != null ? attomProp.getAssessment().getTax().getTaxyear() : null)
                            .assessedValue(attomProp.getAssessment().getAssessed() != null ? attomProp.getAssessment().getAssessed().getAssdttlvalue() : null)
                            .taxAmount(attomProp.getAssessment().getTax() != null ? attomProp.getAssessment().getTax().getTaxamt() : null)
                            .source("ATTOM")
                            .build();
                    newRecords.add(record);
                }
            }
        } catch (Exception e) {
            // log and continue
        }

        if (!newRecords.isEmpty()) {
            taxHistoryRepository.saveAll(newRecords);
        }
        return newRecords;
    }
}
