package com.realestate.due_diligence_agent.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realestate.due_diligence_agent.dto.AttomOwnerResponse;
import com.realestate.due_diligence_agent.entity.OwnershipRecord;
import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.exception.PropertyNotFoundException;
import com.realestate.due_diligence_agent.repository.OwnershipRecordRepository;
import com.realestate.due_diligence_agent.repository.PropertyRepository;

@Service
public class PropertyDataService {

    private final PropertyRepository propertyRepository;
    private final OwnershipRecordRepository ownershipRecordRepository;
    private final AttomService attomService;

    public PropertyDataService(
            PropertyRepository propertyRepository,
            OwnershipRecordRepository ownershipRecordRepository,
            AttomService attomService) {

        this.propertyRepository = propertyRepository;
        this.ownershipRecordRepository = ownershipRecordRepository;
        this.attomService = attomService;
    }

    @Transactional
    public OwnershipRecord fetchAndSaveOwnership(Long propertyId) {

        // 1. Find property in our database
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() ->
                        new PropertyNotFoundException(
                                "Property not found with id: " + propertyId));

        // 2. Call ATTOM
        AttomOwnerResponse response =
                attomService.getDetailOwner(property.getAddress());

        // 3. Validate ATTOM response
        if (response == null ||
                response.getProperty() == null ||
                response.getProperty().isEmpty()) {

            throw new RuntimeException(
                    "No ownership data found from ATTOM for property: "
                            + property.getAddress());
        }

        // 4. Get ATTOM property
        AttomOwnerResponse.Property attomProperty =
                response.getProperty().get(0);

        if (attomProperty.getOwner() == null ||
                attomProperty.getOwner().getOwner1() == null) {

            throw new RuntimeException(
                    "Owner information not available from ATTOM");
        }

        // 5. Extract owner information
        String ownerName =
                attomProperty.getOwner()
                        .getOwner1()
                        .getFullname();

        String ownershipType =
                attomProperty.getOwner()
                        .getOwnerrelationshiprightscode();

        // 6. Create database entity
        OwnershipRecord ownershipRecord =
                OwnershipRecord.builder()
                        .property(property)
                        .ownerName(ownerName)
                        .ownershipType(ownershipType)
                        .acquisitionDate(null)
                        .paymentStatus(null)
                        .build();

        // 7. Save to PostgreSQL
        return ownershipRecordRepository.save(ownershipRecord);
    }
}