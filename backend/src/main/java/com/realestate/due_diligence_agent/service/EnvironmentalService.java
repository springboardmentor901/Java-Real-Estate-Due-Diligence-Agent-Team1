package com.realestate.due_diligence_agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.realestate.due_diligence_agent.client.EnvironmentalClient;
import com.realestate.due_diligence_agent.entity.EnvironmentalRecord;
import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.repository.EnvironmentalRecordRepository;
import com.realestate.due_diligence_agent.repository.PropertyRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EnvironmentalService {

    private final EnvironmentalRecordRepository environmentalRecordRepository;
    private final PropertyRepository propertyRepository;
    private final EnvironmentalClient environmentalClient;

    public EnvironmentalService(EnvironmentalRecordRepository environmentalRecordRepository,
                                PropertyRepository propertyRepository,
                                EnvironmentalClient environmentalClient) {
        this.environmentalRecordRepository = environmentalRecordRepository;
        this.propertyRepository = propertyRepository;
        this.environmentalClient = environmentalClient;
    }

    public List<EnvironmentalRecord> getEnvironmentalRecords(Long propertyId) {
        List<EnvironmentalRecord> existing = environmentalRecordRepository.findByPropertyId(propertyId);
        if (existing != null && !existing.isEmpty()) {
            return existing;
        }

        Optional<Property> propertyOpt = propertyRepository.findById(propertyId);
        if (propertyOpt.isEmpty()) {
            return new ArrayList<>();
        }

        Property property = propertyOpt.get();
        List<EnvironmentalRecord> newRecords = new ArrayList<>();

        try {
            JsonNode response = environmentalClient.getEnvironmentalRecords(property.getAddress());
            if (response != null && response.has("Results") && response.get("Results").isArray()) {
                for (JsonNode result : response.get("Results")) {
                    EnvironmentalRecord record = EnvironmentalRecord.builder()
                            .property(property)
                            .recordType(result.has("RegistryId") ? result.get("RegistryId").asText() : "Unknown")
                            .description(result.has("PrimaryName") ? result.get("PrimaryName").asText() : null)
                            .source("EPA Envirofacts")
                            .build();
                    newRecords.add(record);
                }
            }
        } catch (Exception e) {
            // log and continue
        }

        if (!newRecords.isEmpty()) {
            environmentalRecordRepository.saveAll(newRecords);
        }
        return newRecords;
    }
}
