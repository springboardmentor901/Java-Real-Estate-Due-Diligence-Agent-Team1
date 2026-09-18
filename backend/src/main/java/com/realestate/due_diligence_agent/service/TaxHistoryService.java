package com.realestate.due_diligence_agent.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realestate.due_diligence_agent.dto.AttomAssessmentResponse;
import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.entity.TaxHistory;
import com.realestate.due_diligence_agent.exception.PropertyNotFoundException;
import com.realestate.due_diligence_agent.repository.PropertyRepository;
import com.realestate.due_diligence_agent.repository.TaxHistoryRepository;

@Service
public class TaxHistoryService {

    private final TaxHistoryRepository taxHistoryRepository;
    private final PropertyRepository propertyRepository;
    private final AttomService attomService;

    public TaxHistoryService(
            TaxHistoryRepository taxHistoryRepository,
            PropertyRepository propertyRepository,
            AttomService attomService) {

        this.taxHistoryRepository = taxHistoryRepository;
        this.propertyRepository = propertyRepository;
        this.attomService = attomService;
    }

    @Transactional(noRollbackFor = Exception.class)
    public List<TaxHistory> getTaxHistory(Long propertyId) {

        // 1. Check whether tax data already exists
        List<TaxHistory> existingRecords =
                taxHistoryRepository.findByPropertyId(propertyId);

        if (!existingRecords.isEmpty()) {
            return existingRecords;
        }

        // 2. Find property
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() ->
                        new PropertyNotFoundException(
                                "Property not found with id: " + propertyId));

        // 3. Call ATTOM Assessment API
        AttomAssessmentResponse response =
                attomService.getAssessment(property.getAddress());

        // 4. Validate ATTOM response
        if (response == null ||
                response.getProperty() == null ||
                response.getProperty().isEmpty()) {

            throw new RuntimeException(
                    "No assessment data found from ATTOM for property: "
                            + property.getAddress());
        }

        // 5. Get ATTOM property
        AttomAssessmentResponse.Property attomProperty =
                response.getProperty().get(0);

        if (attomProperty.getAssessment() == null) {

            throw new RuntimeException(
                    "Assessment information not available from ATTOM");
        }

        // 6. Extract assessment
        AttomAssessmentResponse.Assessment assessment =
                attomProperty.getAssessment();

        if (assessment.getAssessed() == null ||
                assessment.getTax() == null) {

            throw new RuntimeException(
                    "Tax or assessed value information not available from ATTOM");
        }

        // 7. Extract values
        Integer taxYear =
                assessment.getTax().getTaxyear();

        var assessedValue =
                assessment.getAssessed().getAssdttlvalue();

        var taxAmount =
                assessment.getTax().getTaxamt();

        // 8. Create TaxHistory entity
        TaxHistory taxHistory =
                TaxHistory.builder()
                        .property(property)
                        .taxYear(taxYear)
                        .assessedValue(assessedValue)
                        .taxAmount(taxAmount)
                        .paymentStatus(null)
                        .build();

        // 9. Save to PostgreSQL
        TaxHistory savedRecord =
                taxHistoryRepository.save(taxHistory);

        return List.of(savedRecord);
    }
}