package com.realestate.due_diligence_agent.service;

import com.realestate.due_diligence_agent.client.AttomClient;
import com.realestate.due_diligence_agent.client.RegridClient;
import com.realestate.due_diligence_agent.dto.AttomDetailOwnerResponse;
import com.realestate.due_diligence_agent.entity.OwnershipRecord;
import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.repository.OwnershipRecordRepository;
import com.realestate.due_diligence_agent.repository.PropertyRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OwnershipService {

    private final OwnershipRecordRepository ownershipRecordRepository;
    private final PropertyRepository propertyRepository;
    private final AttomClient attomClient;
    private final RegridClient regridClient;

    public OwnershipService(OwnershipRecordRepository ownershipRecordRepository,
                            PropertyRepository propertyRepository,
                            AttomClient attomClient,
                            RegridClient regridClient) {
        this.ownershipRecordRepository = ownershipRecordRepository;
        this.propertyRepository = propertyRepository;
        this.attomClient = attomClient;
        this.regridClient = regridClient;
    }

    public List<OwnershipRecord> getOwnershipRecords(Long propertyId) {
        List<OwnershipRecord> existing = ownershipRecordRepository.findByPropertyId(propertyId);
        if (existing != null && !existing.isEmpty()) {
            return existing;
        }

        Optional<Property> propertyOpt = propertyRepository.findById(propertyId);
        if (propertyOpt.isEmpty()) {
            return new ArrayList<>();
        }

        Property property = propertyOpt.get();
        List<OwnershipRecord> newRecords = new ArrayList<>();

        try {
            AttomDetailOwnerResponse response = attomClient.getDetailOwner(property.getAddress());
            if (response != null && response.getProperty() != null && !response.getProperty().isEmpty()) {
                AttomDetailOwnerResponse.Property attomProp = response.getProperty().get(0);
                if (attomProp.getOwner() != null) {
                    OwnershipRecord record = OwnershipRecord.builder()
                            .property(property)
                            .ownerName(attomProp.getOwner().getOwner1first() + " " + attomProp.getOwner().getOwner1last())
                            .ownershipType(attomProp.getOwner().getOwnertypedesc())
                            .source("ATTOM")
                            .build();
                    newRecords.add(record);
                }
            }
        } catch (Exception e) {
            // log and continue
        }

        // If ATTOM fails or returns nothing, we could try Regrid, but for now we just save what we have.
        if (!newRecords.isEmpty()) {
            ownershipRecordRepository.saveAll(newRecords);
        }
        return newRecords;
    }
}
