package com.realestate.due_diligence_agent.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realestate.due_diligence_agent.dto.AttomOwnerResponse;
import com.realestate.due_diligence_agent.entity.OwnershipRecord;
import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.exception.PropertyNotFoundException;
import com.realestate.due_diligence_agent.repository.OwnershipRecordRepository;
import com.realestate.due_diligence_agent.repository.PropertyRepository;

import java.time.LocalDate;
import java.util.List;

import tools.jackson.databind.JsonNode;

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

        // 2. Call ATTOM Detail Owner
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

        // 6. Extract acquisition/sale date from ATTOM Basic Profile
        LocalDate acquisitionDate = null;
        try {
            JsonNode basicProfile = attomService.getBasicProfile(property.getAddress());
            if (basicProfile != null && basicProfile.hasNonNull("property")
                    && basicProfile.get("property").isArray()
                    && !basicProfile.get("property").isEmpty()) {

                JsonNode propNode = basicProfile.get("property").get(0);
                JsonNode saleNode = propNode.get("sale");
                if (saleNode != null && !saleNode.isNull()) {
                    String dateStr = null;
                    if (saleNode.hasNonNull("saleTransDate") && !saleNode.get("saleTransDate").asText().isBlank()) {
                        dateStr = saleNode.get("saleTransDate").asText().trim();
                    } else if (saleNode.hasNonNull("saleAmountData")
                            && saleNode.get("saleAmountData").hasNonNull("saleRecDate")
                            && !saleNode.get("saleAmountData").get("saleRecDate").asText().isBlank()) {
                        dateStr = saleNode.get("saleAmountData").get("saleRecDate").asText().trim();
                    }

                    if (dateStr != null && !dateStr.isEmpty()) {
                        try {
                            String cleanDate = dateStr.length() >= 10 ? dateStr.substring(0, 10) : dateStr;
                            acquisitionDate = LocalDate.parse(cleanDate);
                        } catch (Exception ignored) {
                            // Leave null if date is unparseable
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Failed to retrieve or parse basic profile sale date, acquisitionDate remains null
        }

        // 7. Find existing or create new OwnershipRecord
        List<OwnershipRecord> existingList = ownershipRecordRepository.findByPropertyId(propertyId);
        OwnershipRecord ownershipRecord;
        if (!existingList.isEmpty()) {
            ownershipRecord = existingList.get(0);
            ownershipRecord.setOwnerName(ownerName);
            ownershipRecord.setOwnershipType(ownershipType);
            ownershipRecord.setAcquisitionDate(acquisitionDate);
        } else {
            ownershipRecord = OwnershipRecord.builder()
                    .property(property)
                    .ownerName(ownerName)
                    .ownershipType(ownershipType)
                    .acquisitionDate(acquisitionDate)
                    .paymentStatus(null)
                    .build();
        }

        // 8. Save to PostgreSQL
        return ownershipRecordRepository.save(ownershipRecord);
    }
}