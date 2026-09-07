package com.realestate.due_diligence_agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.realestate.due_diligence_agent.client.PermitsClient;
import com.realestate.due_diligence_agent.entity.Permit;
import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.repository.PermitRepository;
import com.realestate.due_diligence_agent.repository.PropertyRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PermitsService {

    private final PermitRepository permitRepository;
    private final PropertyRepository propertyRepository;
    private final PermitsClient permitsClient;

    public PermitsService(PermitRepository permitRepository,
                          PropertyRepository propertyRepository,
                          PermitsClient permitsClient) {
        this.permitRepository = permitRepository;
        this.propertyRepository = propertyRepository;
        this.permitsClient = permitsClient;
    }

    public List<Permit> getPermits(Long propertyId) {
        List<Permit> existing = permitRepository.findByPropertyId(propertyId);
        if (existing != null && !existing.isEmpty()) {
            return existing;
        }

        Optional<Property> propertyOpt = propertyRepository.findById(propertyId);
        if (propertyOpt.isEmpty()) {
            return new ArrayList<>();
        }

        Property property = propertyOpt.get();
        List<Permit> newRecords = new ArrayList<>();

        try {
            JsonNode response = permitsClient.getPermits(property.getAddress());
            if (response != null && response.isArray()) {
                for (JsonNode node : response) {
                    Permit record = Permit.builder()
                            .property(property)
                            .permitNumber(node.has("permit_number") ? node.get("permit_number").asText() : null)
                            .permitType(node.has("type") ? node.get("type").asText() : null)
                            .status(node.has("status") ? node.get("status").asText() : null)
                            .description(node.has("description") ? node.get("description").asText() : null)
                            .build();
                    newRecords.add(record);
                }
            }
        } catch (Exception e) {
            // log and continue
        }

        if (!newRecords.isEmpty()) {
            permitRepository.saveAll(newRecords);
        }
        return newRecords;
    }
}
