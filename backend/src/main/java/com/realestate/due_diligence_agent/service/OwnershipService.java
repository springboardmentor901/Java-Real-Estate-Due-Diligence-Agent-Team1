package com.realestate.due_diligence_agent.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realestate.due_diligence_agent.entity.OwnershipRecord;
import com.realestate.due_diligence_agent.repository.OwnershipRecordRepository;

@Service
public class OwnershipService {

    private final OwnershipRecordRepository ownershipRecordRepository;
    private final PropertyDataService propertyDataService;

    public OwnershipService(
            OwnershipRecordRepository ownershipRecordRepository,
            PropertyDataService propertyDataService) {

        this.ownershipRecordRepository = ownershipRecordRepository;
        this.propertyDataService = propertyDataService;
    }

    @Transactional
    public List<OwnershipRecord> getOwnershipRecords(Long propertyId) {

        // Check whether ownership data already exists
        List<OwnershipRecord> existingRecords =
                ownershipRecordRepository.findByPropertyId(propertyId);

        // If data already exists, return it
        if (!existingRecords.isEmpty()) {
            return existingRecords;
        }

        // Otherwise fetch owner information from ATTOM
        OwnershipRecord newRecord =
                propertyDataService.fetchAndSaveOwnership(propertyId);

        return List.of(newRecord);
    }
}